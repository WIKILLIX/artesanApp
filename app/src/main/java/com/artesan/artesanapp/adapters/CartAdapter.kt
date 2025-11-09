package com.artesan.artesanapp.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.CartItem

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProduct: ImageView = itemView.findViewById(R.id.imageCartProduct)
        val textName: TextView = itemView.findViewById(R.id.textCartProductName)
        val textPrice: TextView = itemView.findViewById(R.id.textCartProductPrice)
        val textQuantity: TextView = itemView.findViewById(R.id.textQuantity)
        val buttonDecrease: Button = itemView.findViewById(R.id.buttonDecrease)
        val buttonIncrease: Button = itemView.findViewById(R.id.buttonIncrease)
        val buttonRemove: ImageButton = itemView.findViewById(R.id.buttonRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        // Set product image from Base64
        try {
            val imageBytes = Base64.decode(cartItem.product.imageBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.imageProduct.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // Set a placeholder or default image if decoding fails
            holder.imageProduct.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.textName.text = cartItem.product.name
        holder.textPrice.text = String.format("$%.2f", cartItem.product.price)
        holder.textQuantity.text = cartItem.quantity.toString()

        // Decrease quantity button
        holder.buttonDecrease.setOnClickListener {
            val newQuantity = cartItem.quantity - 1
            if (newQuantity > 0) {
                onQuantityChange(cartItem, newQuantity)
            } else {
                onRemove(cartItem)
            }
        }

        // Increase quantity button
        holder.buttonIncrease.setOnClickListener {
            val newQuantity = cartItem.quantity + 1
            if (newQuantity <= cartItem.product.stock) {
                onQuantityChange(cartItem, newQuantity)
            }
        }

        // Remove button
        holder.buttonRemove.setOnClickListener {
            onRemove(cartItem)
        }

        // Disable increase if at max stock
        holder.buttonIncrease.isEnabled = cartItem.quantity < cartItem.product.stock
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCart(newCartItems: List<CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}
