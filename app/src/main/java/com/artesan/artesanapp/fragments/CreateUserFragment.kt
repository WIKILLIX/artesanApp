package com.artesan.artesanapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.models.UserRole
import com.artesan.artesanapp.storage.StorageManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class CreateUserFragment : Fragment() {

    private lateinit var storageManager: StorageManager
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var spinnerRole: AppCompatAutoCompleteTextView
    private lateinit var btnCreateUser: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageManager = StorageManager(requireContext())

        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        spinnerRole = view.findViewById(R.id.spinnerRole)
        btnCreateUser = view.findViewById(R.id.btnCreateUser)

        setupRoleSpinner()
        setupCreateButton()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("USER", "ADMIN")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRole.setAdapter(adapter)
        spinnerRole.setText("USER", false)
    }

    private fun setupCreateButton() {
        btnCreateUser.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val name = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val roleStr = spinnerRole.text.toString()

        // Validaciones
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el nombre completo", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa el email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Ingresa un email válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa la contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el email ya existe
        if (storageManager.emailExists(email)) {
            Toast.makeText(requireContext(), "El email ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear username a partir del email (parte antes del @)
        val username = email.substringBefore("@")

        // Verificar si el username ya existe, si es así, agregar un número
        var finalUsername = username
        var counter = 1
        while (storageManager.usernameExists(finalUsername)) {
            finalUsername = "$username$counter"
            counter++
        }

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.USER
        }

        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            username = finalUsername,
            password = password,
            role = role
        )

        if (storageManager.saveUser(newUser)) {
            Toast.makeText(requireContext(), "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al crear usuario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        storageManager.close()
    }
}
