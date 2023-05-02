package com.example.todo_list.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todo_list.R
import com.example.todo_list.databinding.FragmentSignInBinding
import com.example.todo_list.databinding.FragmentSingUpBinding
import com.example.todo_list.services.Env
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


class SignInFragment : Fragment() {
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignInBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
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
        binding.loginbtn.setOnClickListener{
            val login = binding.username.text.toString().trim()
            val pass = binding.password.text.toString().trim()

            if(login.isNotEmpty() && pass.isNotEmpty()){
                val result = runBlocking { login(login, pass) }
                if (result)
                    navControl.navigate(R.id.action_signInFragment_to_homeFragment)
            }
            else{
                Toast.makeText(activity, "Sprawdź poprawność danych logowania", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerformbtn.setOnClickListener{
            navControl.navigate(R.id.action_signInFragment_to_singUpFragment)
        }
    }

    suspend fun login(username: String, password: String) : Boolean {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post(Env.API_HOST + "/Login?username=" + username + "&password=" + password)
        val json = JSONObject(String(response.body<ByteArray>()))

        if (json["type"] == "SUCCESS"){
            Toast.makeText(activity, "Zalogowano pomyślnie!", Toast.LENGTH_SHORT).show()
            return true
        }
        else {
            Toast.makeText(activity, json["message"].toString(), Toast.LENGTH_SHORT).show()
            return false
        }
    }
}