package com.example.todo_list.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentAddTodoPopupBinding
import com.google.android.material.textfield.TextInputEditText

class AddTodoPopupFragment : DialogFragment() {

    private lateinit var binding : FragmentAddTodoPopupBinding
    private lateinit var listener : DialogNextBtnClickListener

    fun setListener(listener: DialogNextBtnClickListener){
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddTodoPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerEvents()
    }

    private fun registerEvents(){
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            if (todoTask.isNotEmpty()){
                listener.onSaveTask(todoTask, binding.todoEt)
            }else{
                Toast.makeText(context, "Wpisz zadanie", Toast.LENGTH_SHORT).show()
            }
        }

        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }

    interface DialogNextBtnClickListener
    {
        fun onSaveTask(todo: String, todoEt: TextInputEditText)
    }
}