package unit.lhn.callinfo

import android.annotation.TargetApi
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog.*
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.widget.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    var userInfo: UserInfo? = null
    var dateTime = DateTime()
    @TargetApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       inbtn.setOnClickListener({ v ->
           addCall2(CallType.In)
       })
        outbtn.setOnClickListener({ v ->
            addCall2(CallType.Out)
        })
        name.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
        number.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
        date.setOnClickListener(View.OnClickListener {
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener({ view, year, month, dayOfMonth ->
                    date.text = "${String.format("%04d", year)}-${String.format("%02d", month + 1)}-${String.format(
                        "%02d",
                        dayOfMonth
                    )}"
                    dateTime.day = dayOfMonth
                    dateTime.month = month
                    dateTime.year = year
                }),
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        })
        time.setOnClickListener(View.OnClickListener {
            TimePickerDialog(
                this, TimePickerDialog.OnTimeSetListener({ view, hourOfDay, minute ->
                    time.text = "${String.format("%02d", hourOfDay)}:${String.format("%02d", minute)}"
                    dateTime.hour = hourOfDay
                    dateTime.minute = minute

                }),
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE),
                true
            ).show()
        })
    }

    fun addCall2(type: CallType) {
        try {
            if (userInfo == null) {
                Toast.makeText(this, "请选择一个电话号码", Toast.LENGTH_LONG)
                return
            }
            var _date = date.text.toString()
            var _time = time.text.toString()
            if (_date == "" || _time == "") {
                Toast.makeText(this, "设置好时间", Toast.LENGTH_LONG)
                return
            }
            addCall(
                userInfo!!,
                type,
                Calendar.getInstance().apply {
                    this.set(
                        dateTime.year,
                        dateTime.month,
                        dateTime.day,
                        dateTime.hour,
                        dateTime.minute,
                        dateTime.second
                    )
                }.time
            )
        }catch (exception:Exception){
            Toast.makeText(this,"发生错误",Toast.LENGTH_LONG).show()
            info.text.insert(0,exception.message)
        }
        Toast.makeText(this,"插入成功",Toast.LENGTH_LONG).show()
    }

    fun addCall(userInfo: UserInfo, type: CallType, date: Date) {
        var max = 30 * 60
        var min = 60
        var value = ContentValues()
        value.put(Calls.CACHED_NAME, userInfo.name)
        value.put(Calls.TYPE, if (type == CallType.In) Calls.INCOMING_TYPE else Calls.OUTGOING_TYPE)
        value.put(Calls.DURATION, Random().nextInt((max - min) + 1) + 1)
        value.put(Calls.NUMBER, userInfo.phone)
        value.put(Calls.DATE, date.time)
        value.put(Calls.COUNTRY_ISO, "CN")
        value.put(Calls.CACHED_NUMBER_TYPE, 2)
        value.put(Calls.CACHED_LOOKUP_URI, userInfo.uri.toString())
        value.put(Calls.CACHED_MATCHED_NUMBER, userInfo.phone)
        value.put(Calls.CACHED_FORMATTED_NUMBER, userInfo.phone)
        value.put(Calls.IS_READ, if (type == CallType.In) 1 else null)

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

        name.text = userInfo?.name
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
        info.text.insert(0, s)
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

data class DateTime(
    var year: Int = 0,
    var month: Int = 0,
    var day: Int = 0,
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = Random().nextInt(60)
)

enum class CallType {
    In, Out
}