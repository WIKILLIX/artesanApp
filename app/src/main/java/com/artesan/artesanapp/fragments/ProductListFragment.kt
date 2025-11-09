package com.artesan.artesanapp.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.artesan.artesanapp.R
import com.artesan.artesanapp.adapters.ProductAdapter
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.repositories.ProductRepository
import com.artesan.artesanapp.services.AuthService
import com.artesan.artesanapp.services.CartService
import com.artesan.artesanapp.utils.ImageHelper

class ProductListFragment : Fragment() {

    private lateinit var productRepository: ProductRepository
    private lateinit var cartService: CartService
    private lateinit var authService: AuthService
    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var adapter: ProductAdapter
    private var editDialogImageBase64: String? = null

    // Image picker for edit dialog
    private val editImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleEditImageSelection(it)
        }
    }

    private var currentEditImageView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productRepository = ProductRepository(requireContext())
        cartService = CartService(requireContext())
        authService = AuthService(requireContext())

        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        textEmpty = view.findViewById(R.id.textEmptyProducts)

        setupRecyclerView()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                navigateToProductDetail(product)
            }
        )

        recyclerView.adapter = adapter
    }

    private fun navigateToProductDetail(product: Product) {
        val detailFragment = ProductDetailFragment.newInstance(product.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadProducts() {
        val products = productRepository.getAll()

        if (products.isEmpty()) {
            recyclerView.visibility = View.GONE
            textEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            textEmpty.visibility = View.GONE
            adapter.updateProducts(products)
        }
    }

    private fun addToCart(product: Product) {
        cartService.addToCart(product, 1)
            .onSuccess {
                Toast.makeText(requireContext(), "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
            }
            .onFailure { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleEditImageSelection(uri: Uri) {
        editDialogImageBase64 = ImageHelper.uriToBase64(requireContext(), uri)

        if (editDialogImageBase64 != null) {
            val bitmap = ImageHelper.base64ToBitmap(editDialogImageBase64!!)
            currentEditImageView?.setImageBitmap(bitmap)
            Toast.makeText(requireContext(), "Imagen actualizada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditDialog(product: Product) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_product, null)

        val editName = dialogView.findViewById<EditText>(R.id.editProductName)
        val editDescription = dialogView.findViewById<EditText>(R.id.editProductDescription)
        val editPrice = dialogView.findViewById<EditText>(R.id.editProductPrice)
        val editStock = dialogView.findViewById<EditText>(R.id.editProductStock)
        val imageEditPreview = dialogView.findViewById<ImageView>(R.id.imageEditPreview)
        val buttonChangeImage = dialogView.findViewById<Button>(R.id.buttonChangeImage)

        editName.setText(product.name)
        editDescription.setText(product.description)
        editPrice.setText(product.price.toString())
        editStock.setText(product.stock.toString())

        // Set current image or placeholder
        editDialogImageBase64 = product.imageBase64
        if (product.imageBase64 != null) {
            val bitmap = ImageHelper.base64ToBitmap(product.imageBase64)
            imageEditPreview.setImageBitmap(bitmap)
        } else {
            imageEditPreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        currentEditImageView = imageEditPreview

        buttonChangeImage.setOnClickListener {
            editImagePickerLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val updatedProduct = product.copy(
                    name = editName.text.toString(),
                    description = editDescription.text.toString(),
                    price = editPrice.text.toString().toDoubleOrNull() ?: product.price,
                    stock = editStock.text.toString().toIntOrNull() ?: product.stock,
                    imageBase64 = editDialogImageBase64
                )

                productRepository.update(updatedProduct)
                    .onSuccess {
                        Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                        loadProducts()
                    }
                    .onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de eliminar ${product.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                productRepository.delete(product.id)
                    .onSuccess {
                        Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                        loadProducts()
                    }
                    .onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }
}
