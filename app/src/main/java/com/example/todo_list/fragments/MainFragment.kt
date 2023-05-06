package com.example.todo_list.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.marginTop
import androidx.core.view.setMargins
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentMainViewBinding
import com.example.todo_list.databinding.FragmentSignInBinding
import com.example.todo_list.services.Env
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.Identity.encode
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.nio.charset.Charset

class MainFragment : Fragment() {
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentMainViewBinding

    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainViewBinding.inflate(inflater, container, false)
        var inf = binding.root
        fetchLists(inf.findViewById<LinearLayout>(R.id.layout_lists))
        return inf
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }

    private fun init(view: View){
        navControl = Navigation.findNavController(view)
    }

    private fun registerEvents() {
        // Button: Logout
        binding.btnLogout.setOnClickListener{
            navControl.navigate(R.id.action_mainFragment_to_signInFragment)
        }

        // Button: Add list
        binding.btnAddlist.setOnClickListener{
            dialogAddList()
        }
    }

    private fun fetchLists(layout: LinearLayout) {
        val lists = runBlocking { getLists() }
        for(list in lists) {
            val textView = TextView(activity)
            textView.text = list.second
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
            textView.setTextColor(Color.parseColor("#ffffff"))
            textView.setPadding(15,15,15,15)
            textView.setOnClickListener {
                openList(list.first)
            }
            textView.setOnLongClickListener {
                dialogDeleteList(list.first, list.second)
                true
            }
            layout.addView(textView)
        }
    }

    suspend fun getLists() : Array<Pair<Int, String>> {
        var result = arrayOf<Pair<Int, String>>()

        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/GetLists?userId=" + userId)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            val lists = JSONArray(json["message"].toString())
            for (i in 0 until lists.length()) {
                val jsonList = lists.getJSONObject(i)
                val list = Pair<Int, String>(jsonList["id"].toString().toInt(), jsonList["name"].toString())
                result = result.plus(list)
            }
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }

        println("For userId " + userId + " downloaded array of "+ result.size + " lists")

        return result
    }

    suspend fun deleteList(listId: Int) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/ListDelete?listId=" + listId)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Pomyślnie usunięto listę", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun addList(name: String) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/ListAdd?userId=" + userId + "&name=" + name.replace(" ", "%20"))
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Pomyślnie dodano nową listę", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openList(listId: Int) {
        navControl.navigate(R.id.action_mainFragment_to_homeFragment, bundleOf("listId" to listId, "userId" to userId))
    }

    private fun dialogAddList() {
        val alertDialog: AlertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("Dodaj listę")
        val editText = EditText(activity)
        alertDialog.setView(editText)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "DODAJ", DialogInterface.OnClickListener { dialog, id ->
            var name = editText.text.toString()
            var result = runBlocking { addList(name) }
            dialog.dismiss()
            activity?.recreate()
        })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ANULUJ", DialogInterface.OnClickListener { dialog, id ->
            dialog.dismiss()
        })
        alertDialog.show()
    }

    private fun dialogDeleteList(listId: Int, name: String) {
        val alertDialog: AlertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("Dodaj listę")
        alertDialog.setMessage("Czy na pewno chcesz usunąć listę '" + name + "'?")
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "USUŃ", DialogInterface.OnClickListener { dialog, id ->
            runBlocking { deleteList(listId) }
            dialog.dismiss()
            activity?.recreate()
        })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ANULUJ", DialogInterface.OnClickListener { dialog, id ->
            dialog.dismiss()
        })
        alertDialog.show()
    }
}