package com.artesan.artesanapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.adapters.CartAdapter
import com.artesan.artesanapp.models.CartItem
import com.artesan.artesanapp.services.CartService

class CartFragment : Fragment() {

    private lateinit var cartService: CartService
    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var textSubtotal: TextView
    private lateinit var textTotal: TextView
    private lateinit var buttonCheckout: Button
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartService = CartService(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewCart)
        textEmpty = view.findViewById(R.id.textEmptyCart)
        textSubtotal = view.findViewById(R.id.textSubtotal)
        textTotal = view.findViewById(R.id.textTotal)
        buttonCheckout = view.findViewById(R.id.buttonCheckout)

        setupRecyclerView()
        setupCheckoutButton()
        loadCart()
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            cartItems = emptyList(),
            onQuantityChange = { cartItem, newQuantity ->
                updateQuantity(cartItem, newQuantity)
            },
            onRemove = { cartItem ->
                removeFromCart(cartItem)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun setupCheckoutButton() {
        buttonCheckout.setOnClickListener {
            showCheckoutConfirmation()
        }
    }

    private fun loadCart() {
        val cart = cartService.getCart()

        if (cart.isEmpty()) {
            recyclerView.visibility = View.GONE
            textEmpty.visibility = View.VISIBLE
            buttonCheckout.isEnabled = false
        } else {
            recyclerView.visibility = View.VISIBLE
            textEmpty.visibility = View.GONE
            buttonCheckout.isEnabled = true
            adapter.updateCart(cart)
        }

        updateSummary()
    }

    private fun updateSummary() {
        val itemCount = cartService.getCartItemCount()
        val total = cartService.getCartTotal()

        // Update subtotal and total (same value for now)
        textSubtotal.text = String.format("$%.2f", total)
        textTotal.text = String.format("$%.2f", total)
    }

    private fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        cartService.updateQuantity(cartItem.product.id, newQuantity)
            .onSuccess {
                loadCart()
            }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromCart(cartItem: CartItem) {
        cartService.removeFromCart(cartItem.product.id)
            .onSuccess {
                Toast.makeText(requireContext(), "Producto eliminado del carrito", Toast.LENGTH_SHORT).show()
                loadCart()
            }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showCheckoutConfirmation() {
        val total = cartService.getCartTotal()

        AlertDialog.Builder(requireContext())
            .setTitle("Finalizar Compra")
            .setMessage("Total a pagar: $${String.format("%.2f", total)}\n\n¿Confirmar compra?")
            .setPositiveButton("Confirmar") { _, _ ->
                processCheckout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun processCheckout() {
        cartService.checkout()
            .onSuccess {
                AlertDialog.Builder(requireContext())
                    .setTitle("Compra Exitosa")
                    .setMessage("Tu pedido ha sido procesado exitosamente")
                    .setPositiveButton("OK") { _, _ ->
                        loadCart()
                    }
                    .setCancelable(false)
                    .show()
            }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadCart()
    }
}
