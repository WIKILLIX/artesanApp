package com.artesan.artesanapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.User
import com.artesan.artesanapp.models.UserRole
import com.google.android.material.button.MaterialButton

class UserAdapter(
    private var users: List<User>,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.txtUserName)
        val txtUserEmail: TextView = view.findViewById(R.id.txtUserEmail)
        val txtUserRole: TextView = view.findViewById(R.id.txtUserRole)
        val btnEditUser: MaterialButton = view.findViewById(R.id.btnEditUser)
        val btnDeleteUser: MaterialButton = view.findViewById(R.id.btnDeleteUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.txtUserName.text = user.name
        holder.txtUserEmail.text = user.email
        holder.txtUserRole.text = user.role.name

        // Configurar color del rol según el tipo
        when (user.role) {
            UserRole.ADMIN -> {
                holder.txtUserRole.setTextColor(Color.parseColor("#FF5722"))
            }
            UserRole.USER -> {
                holder.txtUserRole.setTextColor(Color.parseColor("#757575"))
            }
        }

        holder.btnEditUser.setOnClickListener {
            onEditClick(user)
        }

        holder.btnDeleteUser.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
