package com.example.todo_list.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentSingUpBinding
import com.example.todo_list.services.Env
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import kotlin.math.log


class SignUpFragment : Fragment() {

    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSingUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSingUpBinding.inflate(inflater, container, false)
        return binding.root
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
        binding.registerbtn.setOnClickListener{
            val login = binding.username.text.toString().trim()
            val pass = binding.password.text.toString().trim()
            val repeatpass = binding.repeatPassword.text.toString().trim()

            if(login.isNotEmpty() && pass.isNotEmpty() && repeatpass.isNotEmpty() && pass == repeatpass){
                val result = runBlocking { register(login, pass) }
                if (result)
                    navControl.navigate(R.id.action_singUpFragment_to_signInFragment)
            }
            else{
                Toast.makeText(activity, "Sprawdź poprawność danych rejestracji", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun register(username: String, password: String) : Boolean {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/Register?username=" + username + "&password=" + password)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Zarejestrowano pomyślnie!", Toast.LENGTH_SHORT).show()
            return true
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
            return false
        }
    }
}