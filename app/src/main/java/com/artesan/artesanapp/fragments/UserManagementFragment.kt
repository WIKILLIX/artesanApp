package com.artesan.artesanapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.adapters.UserAdapter
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.services.AuthService
import com.artesan.artesanapp.storage.StorageManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class UserManagementFragment : Fragment() {

    private lateinit var storageManager: StorageManager
    private lateinit var authService: AuthService
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtEmptyUsers: TextView
    private lateinit var etSearchUser: TextInputEditText
    private lateinit var fabAddUser: FloatingActionButton
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_management, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storageManager = StorageManager(requireContext())
        authService = AuthService(requireContext())

        recyclerView = view.findViewById(R.id.rvUsers)
        txtEmptyUsers = view.findViewById(R.id.txtEmptyUsers)
        etSearchUser = view.findViewById(R.id.etSearchUser)
        fabAddUser = view.findViewById(R.id.fabAddUser)

        setupRecyclerView()
        setupSearch()
        setupFab()

        loadUsers()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            users = emptyList(),
            onEditClick = { user ->
                navigateToEditUser(user)
            },
            onDeleteClick = { user ->
                showDeleteConfirmation(user)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFab() {
        fabAddUser.setOnClickListener {
            navigateToCreateUser()
        }
    }

    private fun loadUsers() {
        val users = storageManager.getUsers()
        updateUsersList(users)
    }

    private fun searchUsers(query: String) {
        val users = storageManager.searchUsersByNameOrEmail(query)
        updateUsersList(users)
    }

    private fun updateUsersList(users: List<User>) {
        if (users.isEmpty()) {
            recyclerView.visibility = View.GONE
            txtEmptyUsers.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            txtEmptyUsers.visibility = View.GONE
            adapter.updateUsers(users)
        }
    }

    private fun navigateToCreateUser() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CreateUserFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToEditUser(user: User) {
        val fragment = EditUserFragment.newInstance(user.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showDeleteConfirmation(user: User) {
        // No permitir eliminar el usuario actualmente logueado
        val currentUser = storageManager.getCurrentUser()
        if (currentUser?.id == user.id) {
            Toast.makeText(
                requireContext(),
                "No puedes eliminar tu propio usuario",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteUser(user)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(user: User) {
        if (storageManager.deleteUser(user.id)) {
            Toast.makeText(
                requireContext(),
                "Usuario eliminado exitosamente",
                Toast.LENGTH_SHORT
            ).show()
            loadUsers()
        } else {
            Toast.makeText(
                requireContext(),
                "Error al eliminar usuario",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        storageManager.close()
    }
}
