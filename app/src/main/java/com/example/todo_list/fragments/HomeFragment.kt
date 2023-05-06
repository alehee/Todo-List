package com.example.todo_list.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentHomeBinding
import com.example.todo_list.utils.ToDoAdapter
import com.example.todo_list.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Text

class HomeFragment : Fragment(), AddTodoPopupFragment.DialogNextBtnClickListener,
    ToDoAdapter.ToDoAdapterClicksInterface {
    private var userId: Int = 0
    private lateinit var navController: NavController
    private lateinit var binding: FragmentHomeBinding
    private lateinit var popUpFragment: AddTodoPopupFragment
    private lateinit var adapter: ToDoAdapter
    private lateinit var mList: MutableList<ToDoData>

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
        //tu sie jakos powinno pobierac z bazy w petli for 4 czesc tutorialu 12 min

        //mList.add(task)
    }
    override fun onSaveTask(todo: String, todoEt: TextInputEditText) {
        //tu sie to do bazy powinno wrzucac
        todoEt.text = null //wywalenie tego co napisalismy z popupu
        popUpFragment.dismiss() //zamkniecie popupu
    }

    override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {
        //tu powinismy wywalac task
    }

    override fun onEditTaskBtnClicked(toDoData: ToDoData) {
        TODO("Not yet implemented")
    }
}