package com.artesan.artesanapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.models.UserRole
import com.artesan.artesanapp.storage.StorageManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditUserFragment : Fragment() {

    private lateinit var storageManager: StorageManager
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var spinnerRole: AppCompatAutoCompleteTextView
    private lateinit var btnResetPassword: MaterialButton
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnDeleteUser: MaterialButton

    private var userId: String? = null
    private var currentUser: User? = null

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String): EditUserFragment {
            val fragment = EditUserFragment()
            val args = Bundle()
            args.putString(ARG_USER_ID, userId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(ARG_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageManager = StorageManager(requireContext())

        etFullName = view.findViewById(R.id.etFullName)
        etEmail = view.findViewById(R.id.etEmail)
        spinnerRole = view.findViewById(R.id.spinnerRole)
        btnResetPassword = view.findViewById(R.id.btnResetPassword)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser)

        setupRoleSpinner()
        loadUser()
        setupButtons()
    }

    private fun setupRoleSpinner() {
        val roles = arrayOf("USER", "ADMIN")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        spinnerRole.setAdapter(adapter)
    }

    private fun loadUser() {
        userId?.let { id ->
            storageManager.getUsers().find { it.id == id }?.let { user ->
                currentUser = user
                etFullName.setText(user.name)
                etEmail.setText(user.email)
                spinnerRole.setText(user.role.name, false)
            } ?: run {
                Toast.makeText(requireContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupButtons() {
        btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        btnResetPassword.setOnClickListener {
            showResetPasswordDialog()
        }

        btnDeleteUser.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun saveChanges() {
        val user = currentUser ?: return

        val name = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
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

        // Verificar si el email ya existe (y no es el mismo usuario)
        if (email != user.email && storageManager.emailExists(email)) {
            Toast.makeText(requireContext(), "El email ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.USER
        }

        val updatedUser = user.copy(
            name = name,
            email = email,
            role = role
        )

        if (storageManager.updateUser(updatedUser)) {
            Toast.makeText(requireContext(), "Usuario actualizado exitosamente", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResetPasswordDialog() {
        val user = currentUser ?: return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_reset_password, null)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Restablecer Contraseña")
            .setMessage("Ingresa la nueva contraseña para ${user.name}")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newPassword = etNewPassword.text.toString()
                if (newPassword.isEmpty() || newPassword.length < 6) {
                    Toast.makeText(
                        requireContext(),
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    resetPassword(newPassword)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun resetPassword(newPassword: String) {
        val user = currentUser ?: return

        val updatedUser = user.copy(password = newPassword)

        if (storageManager.updateUser(updatedUser)) {
            Toast.makeText(requireContext(), "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
            currentUser = updatedUser
        } else {
            Toast.makeText(requireContext(), "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation() {
        val user = currentUser ?: return

        // No permitir eliminar el usuario actualmente logueado
        val loggedInUser = storageManager.getCurrentUser()
        if (loggedInUser?.id == user.id) {
            Toast.makeText(
                requireContext(),
                "No puedes eliminar tu propio usuario",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUser()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser() {
        val user = currentUser ?: return

        if (storageManager.deleteUser(user.id)) {
            Toast.makeText(requireContext(), "Usuario eliminado exitosamente", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        } else {
            Toast.makeText(requireContext(), "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        storageManager.close()
    }
}
