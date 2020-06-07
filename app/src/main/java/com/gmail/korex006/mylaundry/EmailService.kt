package com.gmail.korex006.mylaundry

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import com.gmail.korex006.mylaundry.MyLaundryDBContract.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class EmailService(private var auth: Authenticator) {

    private val props = Properties()
//    private val ccList : List<InternetAddress> = listOf( InternetAddress("dvsukpabi@yahoo.com"))

    init {
        // initialise props["mail.user"] = email.from in individual methods

        props["mail.smtp.auth"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = 587
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.ssl.trust"] = "smtp.gmail.com"
        props["mail.mime.charset"] = "UTF-8"
//        props["mail.smtp.timeout"] = 20000

    }

    data class Email(
            val auth: Authenticator,
            val toList: List<InternetAddress>,
            val from: Address,
            val subject: String,
//            val body: String,
            val htmlBody: String,
            val ccList: List<InternetAddress> = listOf(InternetAddress("dvsukpabi@yahoo.com"))
    )

    class UserPassAuthenticator(private val username: String, private val password: String) : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    fun send(email: Email): Boolean {
        props["mail.user"] = email.from
        val msg: Message = MimeMessage(Session.getDefaultInstance(props, email.auth))
        msg.setFrom(email.from)
        msg.sentDate = Calendar.getInstance().time
        msg.setRecipients(Message.RecipientType.TO, email.toList.toTypedArray())
        msg.replyTo = arrayOf(email.from)
        msg.setRecipients(Message.RecipientType.CC, email.ccList.toTypedArray())

        msg.addHeader("X-Mailer", CLIENT_NAME)
        msg.addHeader("Precedence", "bulk")
        msg.subject = email.subject

        msg.setContent(MimeMultipart().apply {
            addBodyPart(MimeBodyPart().apply {
//                setText(email.body, "iso-8859-1")
                setContent(email.htmlBody, "text/html; charset=UTF-8")
            })

        })
        return try {
            Transport.send(msg)
            true
        } catch (e: SendFailedException) {
            print("Message Send Failed")
            false
        }

    }

    fun testConnection(email: String, password: String): Boolean {
        val session: Session = Session.getInstance(props, auth)
        val transport = session.getTransport("smtp")
        var result: Boolean = false
        return try {
//            GlobalScope.async {
            transport.connect()
            print("Connection successful")
            true
//            }
//

        } catch (e: MessagingException) {
            print("Connection failed")
            false
        }

    }

    companion object {
        const val CLIENT_NAME = "Android StackOverflow Programmatic email"
    }
}

class EmailUtility(private val mContext: Activity) {
    private var email: String = ""
    private var password: String = ""

    private var custEmail: String = ""
//    private var htmlMessage : String = ""

    //
//    init {
//        getEmailCreds()
//    }


    fun sendMail(orderId: String) {
        if (getEmailCreds()) {
            val senderMail = "korex006@gmail.com"
            val pickUpDate = orderId.split("_")[1]
            val html = getMailBody(orderId)
            val auth = EmailService.UserPassAuthenticator(email, password)
            val to = listOf(InternetAddress(custEmail))
            val from = InternetAddress(email)
            val mailSubj = "Laundry Order of $pickUpDate"


            val email = EmailService.Email(auth, to, from, mailSubj, html)
            val emailService = EmailService(auth)

            val mailSentSucessfully = GlobalScope.async {
                emailService.send(email)
            }

            runBlocking {
                if (mailSentSucessfully.await()) {
                    val db = MyLaundryDBHelper(mContext).writableDatabase
                    val contentValues = ContentValues()
                    contentValues.put(OrdersListTable.COLUMN_EMAIL, 1)
                    val whereClause = OrdersListTable.COLUMN_ORDER_ID + " = ?"
                    val whereArgs = arrayOf(orderId)
                    db.update(OrdersListTable.TABLE_NAME, contentValues, whereClause, whereArgs)
                } else {
                    Utils.showToastMessage(mContext, "Error!!: E-Mail Send FAILED!!")
                }
            }

        }
    }

    private fun getMailBody(orderId: String): String {
        val message: String


        val db = MyLaundryDBHelper(mContext).readableDatabase
//        String selection = OrdersListTable.COLUMN_ORDER_ID + " =?";
//        String[] selectionArgs = {orderId};
        val selection = OrdersListTable.COLUMN_ORDER_ID + " = ?"
        val selectionArgs = arrayOf(orderId)
        val orderDetails = OrdersDetailsTable.TABLE_NAME
        val orderList = OrdersListTable.TABLE_NAME
        val custList = PeopleInfoTable.TABLE_NAME
//
        val query = ("SELECT " + orderList + "." + OrdersListTable.COLUMN_PERSON_ID + ", "
                + custList + "." + PeopleInfoTable.COLUMN_CUST_NAME + ", "
                + custList + "." + PeopleInfoTable.COLUMN_EMAIL + ", "
                + orderList + "." + OrdersListTable.COLUMN_PICK_UP_DATE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_SERVICE + ", "
//                + orderDetails + "." + OrdersDetailsTable.COLUMN_PACKAGING + ", "
//                + orderDetails + "." + OrdersDetailsTable.COLUMN_STARCH + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_ITEM_TYPE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_QUANTITY + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_PRICE + ", "
                + orderDetails + "." + OrdersDetailsTable.COLUMN_NET_PRICE + ", "
                + orderList + "." + OrdersListTable.COLUMN_DELIVERY_DATE
                + " FROM " + orderDetails
                + " INNER JOIN " + orderList + " USING (" + OrdersListTable.COLUMN_ORDER_ID + ")"
                + " INNER JOIN " + custList + " USING (" + OrdersListTable.COLUMN_PERSON_ID + ")"
                + " WHERE " + OrdersDetailsTable.COLUMN_ORDER_ID + " = '" + orderId + "'")

//        val columns = arrayOf(
//                PeopleInfoTable.COLUMN_CUST_NAME,
//                PeopleInfoTable.COLUMN_EMAIL
//        )


        val cursor = db.rawQuery(query, null)
//                db.query(custList, columns, selection, selectionArgs, null, null, null)
//
        cursor.moveToFirst()
        custEmail = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_EMAIL))
        val custName = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_CUST_NAME))
        val firstName = custName.split(" ")[0]
        val custId = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_PERSON_ID))
//        val branch = cursor.getString(cursor.getColumnIndex(PeopleInfoTable.COLUMN_BRANCH))
        val pickUpDate = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_PICK_UP_DATE))
        val deliveryDate = cursor.getString(cursor.getColumnIndex(OrdersListTable.COLUMN_DELIVERY_DATE))


        val stub = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "table, th, td {" +
                "  border: 1px solid black;" +
                "  border-collapse: collapse;" +
                "}" +
                "td {\n" +
                "  padding: 5px;\n" +
                "  text-align: center;    \n" +
                "}\n" +
                ".leftside { " +
                "  text-align: left;" +
                "  padding-left: 15px;" +
                "}" +
                "</style>\n" +
                "</head> <body> "

        message = "<p> Hi $firstName, <br> " +
                "Your laundry order has been received as shown below. <br><br> " +
                "For payments, please pay into <br> <em>Exinno White Consults First Bank 2028157922</em> <br> "
        val builder = StringBuilder()
        builder.append(stub)
        builder.append("<h1>MyLaundry</h1>")
        builder.append(message)
//        builder.append("<p>\n\n<br>")
//        builder.append("Cust ID : $custId\n<br>")
//        builder.append("Cust Name: $firstName\n<br>")
//        builder.append("Branch: $branch\n<br>")
        builder.append("Pickup date: $pickUpDate <br><br></p>")
//        builder.append("Delivery date: $deliveryDate\n\n<br></p>")

        builder.append("<table>" +
                "<tr>" +
                "<th>Item</th>" +
                "<th>Service Type</th>" +
                "<th>Item Price</th>" +
                "<th>Quantity</th>" +
                "<th>Net Price</th>" +
                "</tr>")


        var totalQty = 0
        var totalAmt = 0
        // Move cursor back to first pos so as to capture all clothes
        cursor.moveToPrevious()
        while (cursor.moveToNext()) {
            val servicePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_SERVICE)
            val itemTypePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_ITEM_TYPE)
            val quatityPos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_QUANTITY)
            val pricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_PRICE)
            val netPricePos = cursor.getColumnIndex(OrdersDetailsTable.COLUMN_NET_PRICE)
            val service = cursor.getString(servicePos)
            val itemType = cursor.getString(itemTypePos)
            val quantity = cursor.getString(quatityPos).toInt()
            totalQty += quantity
            val price = cursor.getString(pricePos)
            val netPrice = cursor.getString(netPricePos).toInt()
            totalAmt += netPrice


            builder.append("<tr>")
            builder.append("<td>$itemType</td>")
            builder.append("<td>$service</td>")
            builder.append("<td>$price</td>")
            builder.append("<td>$quantity</td>")
            builder.append("<td>$netPrice</td>")
            builder.append("</tr>")
        }
        cursor.close()


        builder.append("<tr><td colspan=\"3\" class=\"leftside\">Total:</td>")
        builder.append("<td> $totalQty</td>")
        builder.append("<td> $totalAmt</td></tr>")
        builder.append("</table>")
        builder.append("<p>Thank you for choosing MyLaundry.NG</p>")
        builder.append("</body></html>")

        return builder.toString()

//        return message
    }

    private fun getEmailCreds(): Boolean {
        val sharedPref = mContext
                .getSharedPreferences("login_creds", Context.MODE_PRIVATE)
//        val defaultValue = resources.getInteger(R.integer.saved_high_score_default_key)
        email = sharedPref.getString("company_email", "")!!
        password = sharedPref.getString("email_password", "")!!

        if (email.isEmpty() || password.isEmpty()) {
            UtilsDialog(mContext).showEmailCredsDialog()
            return false
        } else {
            return true
        }

    }

    fun testLoginCreds(email: String, password: String) {
//        val senderMail = "korex006@gmail.com"
        val auth = EmailService.UserPassAuthenticator(email, password)
//        val to = listOf(InternetAddress("adeyanju.akorede1@gmail.com"))
//        val from = InternetAddress(senderMail)
//        val email = EmailService.Email(auth, to, from, "Test Subject", "Hello Body World")
        val emailService = EmailService(auth)


        val connectionSucessful = GlobalScope.async { emailService.testConnection(email, password) }

        runBlocking {
            if (connectionSucessful.await()) {
                val sharedPref = mContext
                        .getSharedPreferences("login_creds", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("company_email", email)
                    putString("email_password", password)
                    commit()
                }
                Utils.hideKeyboard(mContext)
                Utils.showToastMessage(mContext, "Info: Login Details Changed Successfully")
            } else {
                Utils.showToastMessage(mContext, "Error Correcting to Gmail. Check email and password")
                UtilsDialog(mContext).showEmailCredsDialog()
            }

        }


    }
}