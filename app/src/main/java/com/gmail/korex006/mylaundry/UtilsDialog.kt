package com.gmail.korex006.mylaundry

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.datetime.datePicker
import com.gmail.korex006.mylaundry.MyLaundryDBContract.*
import java.util.*

class UtilsDialog(context: Activity) {
    private var mContext: Activity = context

    fun showDateDialog(view: View) {
        val id = view.id
        val tv_pickUpDate = mContext.findViewById(R.id.tv_pickUpDate) as TextView
        val tv_deliveryDate = mContext.findViewById(R.id.tv_deliveryDate) as TextView

        MaterialDialog(mContext).show {

            datePicker { dialog, date ->
                // Use date (Calendar)
                val laundryOrderActivity = LaundryOrderActivity()
                if (id == R.id.tv_pickUpDate) {
                    laundryOrderActivity.setPickUpDate(date)
                    Utils.updateDateText(date, tv_pickUpDate)

//                    // Set Delivery date to 3 days later
//                    var numofDays = 3
//                    val dayofWeek = date.get(Calendar.DAY_OF_WEEK)
//                    when (dayofWeek) {
//                        7 -> numofDays = 4 // Saturday
//                        6 -> numofDays = 5 // Friday deliver on Wednesday
//                        5 -> numofDays = 5 // Thursday deliver on Tuesday
//                        else -> {
//                            // On every other day, we give 3 delivery days
//                            numofDays = 3
//                        }
//                    }
                    val delDate = date.clone() as Calendar
                    val numOfDays = Utils.calcNumofDeliveryDays(date)
                    delDate.add(Calendar.DAY_OF_YEAR, numOfDays)
                    laundryOrderActivity.setDeliveryDate(delDate)
                    Utils.updateDateText(delDate, tv_deliveryDate)
                } else if (id == R.id.tv_deliveryDate) {
                    laundryOrderActivity.setDeliveryDate(date)
                    Utils.updateDateText(date, tv_deliveryDate)
                }
            }
        }
    }

    fun showConfirmSaveDialog(pickUpDate: Calendar, deliveryDate: Calendar, totalquantity: String, totalAmt: String) {
        MaterialDialog(mContext).show {
            title(text = "Save Order?")
//            message(text = "Your Message")
            positiveButton(text = "Confirm save") { dialog ->
                // Do something
                saveOrderConfirmed(pickUpDate, deliveryDate, totalquantity, totalAmt)
            }
            negativeButton(text = "Go Back!")
        }
    }

    private fun saveOrderConfirmed(pickUpDate: Calendar, deliveryDate: Calendar, totalquantity: String, totalAmt: String) {
        Utils.hideKeyboard(mContext)
        val tv_personId = mContext.findViewById<TextView>(R.id.tv_personID)
        val personID: String = tv_personId.text.toString()
//        val dateFormat = SimpleDateFormat(
//                "dd-MM-yyyy", Locale.getDefault())
        val pickUpDateStr = Utils.formatDate(pickUpDate)
//                dateFormat.format(pickUpDate.time)
        val deliveryDateStr = Utils.formatDate(deliveryDate)
//                dateFormat.format(deliveryDate.time)
        val orderId = personID + "_" + pickUpDateStr + "_" + deliveryDateStr
        val db = MyLaundryDBHelper(mContext).writableDatabase
        val selection = OrdersListTable.COLUMN_ORDER_ID + " =?"
        val selectionArgs = arrayOf(orderId)
        val cursor = db.query(OrdersListTable.TABLE_NAME, null, selection, selectionArgs,
                null, null, null)
        val orderValues = ContentValues()
        if (cursor.count == 0) {

            orderValues.put(OrdersListTable.COLUMN_ORDER_ID, orderId)
            orderValues.put(OrdersListTable.COLUMN_PERSON_ID, personID)
            orderValues.put(OrdersListTable.COLUMN_PICK_UP_DATE, pickUpDateStr)
            orderValues.put(OrdersListTable.COLUMN_DELIVERY_DATE, deliveryDateStr)
            orderValues.put(OrdersListTable.COLUMN_QTY, totalquantity)
            orderValues.put(OrdersListTable.COLUMN_AMOUNT, totalAmt)
            db.insert(OrdersListTable.TABLE_NAME, null, orderValues)
        } else {
            // if orderid already exist. Update the total qty and total amt
            cursor.moveToNext()
            val prevTotalQty = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_QTY))
            val prevTotalAmt = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_AMOUNT))

//            val newTotalQty = Integer.parseInt(prevTotalQty) + Integer.parseInt(totalquantity)
//            val newTotalAmt = Integer.parseInt(prevTotalAmt) + Integer.parseInt(totalAmt)
            orderValues.put(OrdersListTable.COLUMN_QTY, totalquantity)
            orderValues.put(OrdersListTable.COLUMN_AMOUNT, totalAmt)

            db.update(OrdersListTable.TABLE_NAME, orderValues, selection, selectionArgs)
            db.delete(OrdersDetailsTable.TABLE_NAME, selection, selectionArgs)
        }
        cursor.close()
        val linearLayout = mContext.findViewById<LinearLayout>(R.id.linear_items)
        for (i in 0 until linearLayout.childCount) {
            val cv = linearLayout.getChildAt(i) as CardView
            val spn_items = cv.findViewById<View>(R.id.spn_item) as Spinner
            val spn_services = cv.findViewById<View>(R.id.spn_service) as Spinner
            val spn_packaging = cv.findViewById<View>(R.id.spn_packaging) as Spinner
            val spn_starch = cv.findViewById<View>(R.id.spn_starch) as Spinner
            val et_price = cv.findViewById<View>(R.id.et_itemPrice) as EditText
            val tv_netPrice = cv.findViewById<View>(R.id.tv_netPrice) as TextView
            val et_quantity = cv.findViewById<View>(R.id.et_quantity) as EditText
            val itemName = spn_items.selectedItem as String
            val service = spn_services.selectedItem as String
            val packaging = spn_packaging.selectedItem as String
            val starch = spn_starch.selectedItem as String
            val price = et_price.text.toString()
            val quantity = et_quantity.text.toString()
            val netPrice = tv_netPrice.text.toString()
            val itemDetails = ContentValues()
            itemDetails.put(OrdersDetailsTable.COLUMN_ORDER_ID, orderId)
            itemDetails.put(OrdersDetailsTable.COLUMN_ITEM_TYPE, itemName)
            itemDetails.put(OrdersDetailsTable.COLUMN_SERVICE, service)
            itemDetails.put(OrdersDetailsTable.COLUMN_PACKAGING, packaging)
            itemDetails.put(OrdersDetailsTable.COLUMN_STARCH, starch)
            itemDetails.put(OrdersDetailsTable.COLUMN_PRICE, price)
            itemDetails.put(OrdersDetailsTable.COLUMN_QUANTITY, quantity)
            itemDetails.put(OrdersDetailsTable.COLUMN_NET_PRICE, netPrice)
            db.insert(OrdersDetailsTable.TABLE_NAME, null, itemDetails)
        }
        Utils.showToastMessage(mContext, "Order Successfully saved")
        showOrderSavedDialog(orderId)
    }

    private fun showOrderSavedDialog(orderId: String) {
        MaterialDialog(mContext).show {
            title(text = "Order Saved successfully!!")
//            message(text = "Your Message")
            positiveButton(text = "Print Order Slip") { dialog ->
                val intent = Intent(mContext, PrintActivity::class.java)
                intent.putExtra(PrintActivity.ORDER_ID, orderId)
                mContext.startActivity(intent)
                mContext.finish()
            }
            negativeButton(text = "Add new Order") {
                val intent = Intent(mContext, SearchActivity::class.java)
                mContext.startActivity(intent)
                mContext.finish()
            }
        }
    }

    fun showJobCardDialog(jobCardStr: String) {
        MaterialDialog(mContext).show {
//            title(text = "Order Saved successfully!!")
            message(text = jobCardStr)
        }
    }

    fun showCreateOrderDetailsFileDialog(adminActivity: AdminActivity) {
        MaterialDialog(mContext).show {
            cancelable(false)  // calls setCancelable on the underlying dialog
            cancelOnTouchOutside(false)  // calls setCanceledOnTouchOutside on the underlying dialog
            title(text = "Initial Save!!")
            message(text = "Order details file doesn't exist yet, Please select folder where it should be saved")
            positiveButton(text = "Ok") { dialog ->
                // Do something
                adminActivity.createFile()
            }

        }
    }

    fun showOrdersDetailsExtractedSucessfullyDialog() {
        MaterialDialog(mContext).show {
            title(text = "Orders Sucessfully extracted!!")
            message(text = "Please check phone file manager for orderDetails.csv file")
        }
    }

    fun showsOrderClickDialog(orderId: String) {
        MaterialDialog(mContext).show {
//            title(text = "Order Saved successfully!!")
//            message(text = "Your Message")
            positiveButton(text = "Print Order Slip") { dialog ->
                val intent = Intent(mContext, PrintActivity::class.java)
                intent.putExtra(PrintActivity.ORDER_ID, orderId)
                mContext.startActivity(intent)
                mContext.finish()
            }
            negativeButton(text = "Edit Order") {
                val intent = Intent(mContext, LaundryOrderActivity::class.java)
                intent.putExtra(LaundryOrderActivity.EXTRA_ORDER_ID, orderId)
                mContext.startActivity(intent)
                mContext.finish()
            }
        }
    }

    fun showOrderExistsDialog(orderId: String, custName: String) {
        MaterialDialog(mContext).show {
            title(text = "Order already exists !!")
            val orderIDSplit = orderId.split("_")
            val message = "An order already exists for " + custName +
                    "\nPickUp date: " + orderIDSplit[1] +
                    "\nDelivery date: " + orderIDSplit[2] +
                    "\n\nPlease go to saved order page to edit the existing order or you have to select different pickup/delivery date to continue "
            message(text = message)
//            positiveButton(text = "Edit existing Order") { dialog ->
//                val intent = Intent(mContext, LaundryOrderActivity::class.java)
//                intent.putExtra(LaundryOrderActivity.EXTRA_ORDER_ID, orderId)
//                mContext.startActivity(intent)
//                mContext.finish();
//            }
//            negativeButton(text = "Edit Order dates") {
////                val intent = Intent(mContext, LaundryOrderActivity::class.java)
////                mContext.startActivity(intent)
////                mContext.finish()
//            }
        }
    }

    fun showDeleteOrderDialog(orderId: String) {

        MaterialDialog(mContext).show {
            title(text = "Delete Order ??")
//            message(text = "Your Message")
            positiveButton(text = "Yes") { dialog ->
                val db = MyLaundryDBHelper(mContext).writableDatabase
                val selection = OrdersListTable.COLUMN_ORDER_ID + " = ?"
                val selectionArgs = arrayOf(orderId)
                db.delete(OrdersDetailsTable.TABLE_NAME, selection, selectionArgs)
                db.delete(OrdersListTable.TABLE_NAME, selection, selectionArgs)
                mContext.finish()
                mContext.startActivity(Intent(mContext, SavedOrdersActivity::class.java))

            }
            negativeButton(text = "No") {

            }
        }

    }

    fun showEmailClickedDialog(orderDetailsArr: Array<String>) {

        val title: String
        val message: String
        val posBtnText: String
        val negBtnText: String

        val orderId = orderDetailsArr[0]

        // Split the orderId into an array and get the first element which is the cust ID
        val custId = orderId.split("_")[0]
        // Split the orderId into an array and get the 2nd element which is the pickUpDate
        val orderDate = orderId.split("_")[1]
        val validEmailExist = orderDetailsArr[1].toInt()
        val emailSent = orderDetailsArr[2].toInt()

        val db = MyLaundryDBHelper(mContext).readableDatabase
//        String selection = OrdersListTable.COLUMN_ORDER_ID + " =?";
//        String[] selectionArgs = {orderId};
        val selection = PeopleInfoTable.COLUMN_PERSON_ID + " = ?"
        val selectionArgs = arrayOf(custId)
        val custList = PeopleInfoTable.TABLE_NAME

        val columns = arrayOf(
                PeopleInfoTable.COLUMN_CUST_NAME,
                PeopleInfoTable.COLUMN_EMAIL
        )


        val cursor = db.query(custList, columns, selection, selectionArgs, null, null, null)
//                db.rawQuery(query, null)
        cursor.moveToFirst()
        val email = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_EMAIL))
        val custName = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME))
        cursor.close()


        if (validEmailExist == 0) {


            if (email.isEmpty()) {
                // There is no valid email because none exists
                title = "No Email Found"
                message = "No email found in our records found for $custName. " +
                        "Please first get a valid mail from customer and give it to the IT Staff to update records"

            } else {
                // Means that there is a email but it isn't valid
                title = "Email not Valid!"
                message = "The email $email found for $custName doesn't seem to be valid " +
                        "Please confirm email address with customer and give it to the IT staff to update records"
            }

            MaterialDialog(mContext).show {
                title(text = title)
                message(text = message)

            }

        } else {
            // Valid email exist for customer in DB

            if (emailSent == 0) {
                // email has not been sent before
                title = "Confirm Send"
                message = "Send mail to $custName for order made on $orderDate ? "
                posBtnText = "Yes"
                negBtnText = "No"
            } else {
                // Valid mail exist and an email has been sent to customer b4
                title = "Resend mail ??"
                message = "An email has been sent previously to $custName for order made on $orderDate"
                posBtnText = "Resend Mail"
                negBtnText = "Go Back"
            }

            MaterialDialog(mContext).show {
                title(text = title)
                message(text = message)
                positiveButton(text = posBtnText) { dialog ->
                    if (isOnline()) {
                        val emailUtility = EmailUtility(mContext)
                        emailUtility.sendMail(orderId)
                    } else {
                        Utils.showToastMessage(mContext, "Error!! Warning please connect to internet first")
                    }

                }
                negativeButton(text = negBtnText) {

                }
            }
        }
    }

    @Suppress("DEPRECATION")
    fun isOnline(): Boolean {
        var connected = false
        @Suppress("LiftReturnOrAssignment")
        (mContext.let {
            val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = cm.activeNetwork ?: return false
                val actNw = cm.getNetworkCapabilities(networkCapabilities) ?: return false
                connected = actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else {
                val netInfo = cm.activeNetworkInfo
                connected = netInfo?.isConnectedOrConnecting == true
            }
        })
        return connected
    }

    fun showEmailCredsDialog() {

        MaterialDialog(mContext).show {
            title(text = "Enter Company Mail and password")
            message(text = "Ensure Data is On !!")
            customView(R.layout.custom_email_password_view)
            positiveButton(text = "Enter Creds") { dialog ->
                val parentView = dialog.getCustomView()
                val et_email = parentView.findViewById<EditText>(R.id.et_email)
                val et_password = parentView.findViewById<EditText>(R.id.et_password)
                val email = et_email.text
                val password = et_password.text
                if (email.isNullOrBlank() || password.isNullOrBlank() || !Utils.validateEmail(email.toString())) {
                    Utils.hideKeyboard(mContext)
                    Utils.showToastMessage(mContext, "Error!! Please enter a valid mail and password")
                    showEmailCredsDialog()
                } else {
                    if (isOnline()) {
                        EmailUtility(mContext).testLoginCreds(
                                email.toString().toLowerCase(Locale.ROOT), password.toString())
                    } else {
                        Utils.showToastMessage(mContext, "Error!! Internet Connection needed to continue!")
                        showEmailCredsDialog()
                    }

                }
            }
        }
    }

    fun resetEmailLoginDialog() {
        MaterialDialog(mContext).show {
            title(text = "Reset Company Mail and password")
            message(text = "This will reset the email login details saved on this App. \n" +
                    "The new company mail and password will requested from you \n" +
                    "You won't be able to send mail to customers till another email and password is provided")
//            noAutoDismiss()
            positiveButton(text = "Yes") { dialog ->
                val sharedPref = mContext
                        .getSharedPreferences("login_creds", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("company_email", "")
                    putString("email_password", "")
                    commit()
                }
                showEmailCredsDialog()

            }
        }
    }

}