package unit.lhn.callinfo

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.provider.CallLog.*
import android.telecom.Call
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.R.attr.data
import android.widget.TextView
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {
    lateinit var name: TextView
    lateinit var number: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.findViewById<Button>(R.id.inbtn).setOnClickListener({ view ->

        })
        this.findViewById<Button>(R.id.outbtn).setOnClickListener(View.OnClickListener {

        })
        name = this.findViewById<TextView>(R.id.name)
        name.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
        number = this.findViewById<TextView>(R.id.name)
        number.setOnClickListener(View.OnClickListener {
            intentToContact()
        })
    }

    fun addCall(number: String, name: String, type: String, date: Date) {
        var max = 30 * 60
        var min = 60
        var value = ContentValues()
        value.put(Calls.CACHED_NAME, name)
        value.put(Calls.TYPE, if (type == "in") Calls.INCOMING_TYPE else Calls.OUTGOING_TYPE)
        value.put(Calls.DURATION, Random().nextInt((max - min) + 1) + 1)
        value.put(Calls.NUMBER, number)
        value.put(Calls.DATE, date.time)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x30) {
            if (data != null) {
                val uri = data.data
                name.text=uri.toString()

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
