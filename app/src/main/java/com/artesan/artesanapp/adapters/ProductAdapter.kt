package com.artesan.artesanapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.utils.ImageHelper

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardProduct: CardView = itemView.findViewById(R.id.cardProduct)
        val imageProduct: ImageView = itemView.findViewById(R.id.imageProduct)
        val textName: TextView = itemView.findViewById(R.id.textProductName)
        val textPrice: TextView = itemView.findViewById(R.id.textProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.textName.text = product.name
        holder.textPrice.text = String.format("$%.2f", product.price)

        // Load product image
        if (product.imageBase64 != null) {
            val bitmap = ImageHelper.base64ToBitmap(product.imageBase64)
            holder.imageProduct.setImageBitmap(bitmap)
        } else {
            // Set placeholder if no image
            holder.imageProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Set click listener for the entire card
        holder.cardProduct.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
