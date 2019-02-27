package unit.lhn.callinfo

import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog.*
import android.text.SpannableStringBuilder
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.widget.*


class MainActivity : AppCompatActivity() {
    lateinit var name: TextView
    lateinit var number: TextView
    lateinit var date: TextView
    lateinit var time: TextView
    var userInfo: UserInfo? = null
    @TargetApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById<Button>(R.id.inbtn).setOnClickListener({ view ->
            if (userInfo == null) {
                Toast.makeText(applicationContext, "请选择一个电话号码", Toast.LENGTH_LONG)
                return@setOnClickListener
            }

        })
        this.findViewById<Button>(R.id.outbtn).setOnClickListener(View.OnClickListener {
            if (userInfo == null) {
                Toast.makeText(applicationContext, "请选择一个电话号码", Toast.LENGTH_LONG)
                return@OnClickListener
            }
        })
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        name = this.findViewById<TextView>(R.id.name)
        name.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
        number = this.findViewById<TextView>(R.id.number)
        number.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
        date.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                applicationContext,
                DatePickerDialog.OnDateSetListener({ view, year, month, dayOfMonth ->


                }),
                0,0,0
                ).show()
        })
        time.setOnClickListener(View.OnClickListener {
            TimePickerDialog(
                applicationContext, TimePickerDialog.OnTimeSetListener({ view, hourOfDay, minute ->


                }),
                12, 12, true
            ).show()
        })
    }

    fun addCall2(userInfo: UserInfo, type: String) {
        if (userInfo == null) {
            Toast.makeText(applicationContext, "请选择一个电话号码", Toast.LENGTH_LONG)
            return
        }
        var _date = date.text.toString()
        var _time = time.text.toString()
        if (_date == "" || _time == "") {
            Toast.makeText(applicationContext, "设置好时间", Toast.LENGTH_LONG)
            return
        }


    }

    fun addCall(userInfo: UserInfo, type: String, date: Date) {
        var max = 30 * 60
        var min = 60
        var value = ContentValues()
        value.put(Calls.CACHED_NAME, userInfo.name)
        value.put(Calls.TYPE, if (type == "in") Calls.INCOMING_TYPE else Calls.OUTGOING_TYPE)
        value.put(Calls.DURATION, Random().nextInt((max - min) + 1) + 1)
        value.put(Calls.NUMBER, userInfo.phone)
        value.put(Calls.DATE, date.time)
        value.put(Calls.COUNTRY_ISO, "CN")
        value.put(Calls.CACHED_NUMBER_TYPE, 2)
        value.put(Calls.CACHED_LOOKUP_URI, userInfo.uri.toString())
        value.put(Calls.CACHED_MATCHED_NUMBER, userInfo.phone)
        value.put(Calls.CACHED_FORMATTED_NUMBER, userInfo.phone)
        value.put(Calls.IS_READ, if (type == "in") 1 else null)

        contentResolver.insert(Calls.CONTENT_URI, value)
    }

    val pp = listOf<String>(
        Calls.TYPE, Calls.NUMBER, Calls.COUNTRY_ISO, Calls.DATE,
        Calls.DURATION, Calls.NEW, Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE,
        Calls.CACHED_NUMBER_LABEL, Calls.IS_READ, Calls.VOICEMAIL_URI,
        Calls.GEOCODED_LOCATION, Calls.CACHED_LOOKUP_URI, Calls.CACHED_MATCHED_NUMBER,
        Calls.CACHED_NORMALIZED_NUMBER, Calls.CACHED_FORMATTED_NUMBER,
        Calls.PHONE_ACCOUNT_ID

    )

    fun intentToContact() {
        // 跳转到联系人界面
        var intent = Intent()
        intent.setAction("android.intent.action.PICK")
        intent.addCategory("android.intent.category.DEFAULT")
        intent.setType("vnd.android.cursor.dir/phone_v2")
        startActivityForResult(intent, 0x30)
    }

    fun setCurrentContanct(uri: Uri) {
        this.userInfo = getContancrInfo(uri)

        name.text = userInfo?.uri.toString()
        number.text = userInfo?.phone
        info.text.insert(0, userInfo.toString())
    }

    fun getContancrInfo(uri: Uri): UserInfo {
        val userInfo = UserInfo(uri = uri)
        val cursor = contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts._ID
            ), null, null, null
        )
        while (cursor.moveToNext()) {
            userInfo.name = cursor.getString(1)
            userInfo.phone = cursor.getString(0)
            userInfo.contractId = cursor.getString(2)
            userInfo.lookupkey = cursor.getString(3)
            break
        }
        userInfo.lookupUri =
            "content://com.android.contacts/contacts/lookup/${userInfo.lookupkey}/${userInfo.contractId}"
        return userInfo
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x30) {
            if (data != null) {
                setCurrentContanct(data.data)

            }
        }
    }

    fun readCall(number: String) {
        var cursor = contentResolver.query(Calls.CONTENT_URI, null, null, null, Calls.DEFAULT_SORT_ORDER)
        var s = ""
        while (cursor.moveToNext()) {
            var number = cursor.getString(cursor.getColumnIndex(Calls.NUMBER))
            if (number == "553" || number == "13542179185") {
                s += pp.map { "[${it}=${getValue(it, cursor)}]\n" }
            }
        }
        findViewById<EditText>(R.id.info).text = SpannableStringBuilder(s)
    }

    fun getValue(columnName: String, cursor: Cursor): String {

        when (cursor.getType(cursor.getColumnIndex(columnName))) {
            Cursor.FIELD_TYPE_BLOB ->
                return cursor.getBlob(cursor.getColumnIndex(columnName)).contentToString()
            Cursor.FIELD_TYPE_STRING ->
                return cursor.getString(cursor.getColumnIndex(columnName))
            Cursor.FIELD_TYPE_INTEGER ->
                return cursor.getLong(cursor.getColumnIndex(columnName)).toString()
            Cursor.FIELD_TYPE_FLOAT ->
                return cursor.getFloat(cursor.getColumnIndex(columnName)).toString()
        }
        return "null"
    }

}

data class UserInfo(
    var uri: Uri,
    var name: String = "",
    var phone: String = "",
    var contractId: String = "",
    var lookupkey: String = "",
    var lookupUri: String = ""
)