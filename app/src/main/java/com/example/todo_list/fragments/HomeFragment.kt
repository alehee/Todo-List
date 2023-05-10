package com.example.todo_list.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentHomeBinding
import com.example.todo_list.services.Env
import com.example.todo_list.utils.ToDoAdapter
import com.example.todo_list.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text

class HomeFragment : Fragment(), AddTodoPopupFragment.DialogNextBtnClickListener,
    ToDoAdapter.ToDoAdapterClicksInterface {
    private var listId: Int = 0
    private var userId: Int = 0
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var popUpFragment: AddTodoPopupFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listId = it.getInt("listId")
            userId = it.getInt("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val inf = inflater.inflate(R.layout.fragment_home, container, false)
//        inf.findViewById<TextView>(R.id.basicText).setText("Logged userId: " + userId)
//        return inf
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        getDataFromDB()
        registerEvents()
    }

    private fun registerEvents(){
        binding.addBtnHome.setOnClickListener {
            popUpFragment = AddTodoPopupFragment()
            popUpFragment!!.setListener(this)
            popUpFragment.show(
                childFragmentManager,
                "AddTodoPopupFragment"
            )
        }
        binding.backBtnHome.setOnClickListener {
            //navController.navigate(R.id.action_homeFragment_to_mainFragment)
            goBackToMain()
        }

        binding.addFriend.setOnClickListener {
            //ale jaja
        }

        binding.refresh.setOnClickListener {
            //jak berety
        }
    }
    private fun init(view: View){
        navController = Navigation.findNavController(view)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        mList = mutableListOf()
        adapter = ToDoAdapter(mList)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter
    }

    private fun getDataFromDB(){
        val result = runBlocking { apiGetTasks() }
        for (task in result) {
            mList.add(task)
        }
    }

    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        runBlocking { apiAddTask(todo) }
        popUpFragment.dismiss()
        activity?.recreate()
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        runBlocking { apiDeleteTask(toDoData) }
        activity?.recreate()
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        val alertDialog: AlertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle("Edytuja taska")
        val editText = EditText(activity)
        editText.setText(toDoData.task)
        alertDialog.setView(editText)
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "EDYTUJ", DialogInterface.OnClickListener { dialog, id ->
            var name = editText.text.toString()
            var result = runBlocking { apiEditTask(ToDoData(toDoData.taskId, name)) }
            dialog.dismiss()
            activity?.recreate()
        })
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ANULUJ", DialogInterface.OnClickListener { dialog, id ->
            dialog.dismiss()
        })
        alertDialog.show()
    }

    // Change view back to main
    private fun goBackToMain() {
        navController.navigate(R.id.action_homeFragment_to_mainFragment, bundleOf("userId" to userId))
    }

    /* API Calls */
    suspend fun apiGetTasks() : Array<ToDoData> {
        var result = arrayOf<ToDoData>()

        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/GetTasks?listId=" + listId)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            val tasks = JSONArray(json["message"].toString())
            for (i in 0 until tasks.length()) {
                val jsonTask = tasks.getJSONObject(i)
                val task = ToDoData(jsonTask["id"].toString().toInt(), jsonTask["name"].toString())
                result = result.plus(task)
            }
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }

        println("For listId " + listId + " downloaded array of "+ result.size + " tasks")

        return result
    }

    suspend fun apiAddTask(name: String) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/TaskAdd?userId=" + userId + "&name=" + name.replace(" ", "%20") + "&listId=" + listId)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Pomyślnie dodano nowy task", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun apiDeleteTask(toDoData: ToDoData) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/TaskDelete?taskId=" + toDoData.taskId)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Pomyślnie usunięto task", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun apiEditTask(toDoData: ToDoData) {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/TaskEdit?taskId=" + toDoData.taskId + "&name=" + toDoData.task.replace(" ", "%20"))
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Pomyślnie edytowano task", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
        }
    }
}
































