package com.gmail.korex006.mylaundry

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersDetailsTable
import com.gmail.korex006.mylaundry.MyLaundryDBContract.OrdersListTable
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

                    // Set Delivery date to 3 days later
                    //TODO: make sure delivery date is 3 WORKING days
                    var numofDays = 3
                    val dayofWeek = date.get(Calendar.DAY_OF_WEEK)
                    when (dayofWeek) {
                        7 -> numofDays = 4 // Saturday
                        6 -> numofDays = 5 // Friday deliver on Wednesday
                        5 -> numofDays = 5 // Thursday deliver on Tuesday
                        else -> {
                            // On every other day, we give 3 delivery days
                            numofDays = 3
                        }
                    }
                    date.add(Calendar.DAY_OF_YEAR, numofDays)
                    laundryOrderActivity.setDeliveryDate(date)
                    Utils.updateDateText(date, tv_deliveryDate)
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
        val personID: String = tv_personId.getText().toString()
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
            cursor.moveToNext();
            val prevTotalQty = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_QTY))
            val prevTotalAmt = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_AMOUNT))

//            val newTotalQty = Integer.parseInt(prevTotalQty) + Integer.parseInt(totalquantity)
//            val newTotalAmt = Integer.parseInt(prevTotalAmt) + Integer.parseInt(totalAmt)
            orderValues.put(OrdersListTable.COLUMN_QTY, totalquantity)
            orderValues.put(OrdersListTable.COLUMN_AMOUNT, totalAmt)

            db.update(OrdersListTable.TABLE_NAME, orderValues, selection, selectionArgs)
            db.delete(OrdersDetailsTable.TABLE_NAME, selection, selectionArgs);
        }
        cursor.close()
        val linearLayout = mContext.findViewById<LinearLayout>(R.id.linear_items)
        for (i in 0 until linearLayout.getChildCount()) {
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
                mContext.finish();
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
                mContext.finish();
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

}