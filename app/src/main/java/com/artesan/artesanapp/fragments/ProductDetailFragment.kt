package com.artesan.artesanapp.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.artesan.artesanapp.R
import com.artesan.artesanapp.models.Product
import com.artesan.artesanapp.repositories.ProductRepository
import com.artesan.artesanapp.services.AuthService
import com.artesan.artesanapp.services.CartService
import com.artesan.artesanapp.utils.ImageHelper

class ProductDetailFragment : Fragment() {

    private lateinit var productRepository: ProductRepository
    private lateinit var cartService: CartService
    private lateinit var authService: AuthService
    private lateinit var imageMain: ImageView
    private lateinit var textName: TextView
    private lateinit var textPrice: TextView
    private lateinit var textDescription: TextView
    private lateinit var textStock: TextView
    private lateinit var buttonBack: ImageButton
    private lateinit var buttonAddToCart: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonDelete: Button
    private lateinit var adminButtons: LinearLayout

    private var product: Product? = null
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
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productRepository = ProductRepository(requireContext())
        cartService = CartService(requireContext())
        authService = AuthService(requireContext())

        // Initialize views
        imageMain = view.findViewById(R.id.imageProductMain)
        textName = view.findViewById(R.id.textProductName)
        textPrice = view.findViewById(R.id.textProductPrice)
        textDescription = view.findViewById(R.id.textProductDescription)
        textStock = view.findViewById(R.id.textProductStock)
        buttonBack = view.findViewById(R.id.buttonBack)
        buttonAddToCart = view.findViewById(R.id.buttonAddToCart)
        buttonEdit = view.findViewById(R.id.buttonEdit)
        buttonDelete = view.findViewById(R.id.buttonDelete)
        adminButtons = view.findViewById(R.id.adminButtons)

        // Get product ID from arguments
        val productId = arguments?.getString("productId")
        if (productId != null) {
            loadProduct(productId)
        } else {
            Toast.makeText(requireContext(), "Error: Product not found", Toast.LENGTH_SHORT).show()
            goBack()
        }

        setupButtons()
        setupAdminVisibility()
    }

    private fun loadProduct(productId: String) {
        product = productRepository.getById(productId)

        product?.let {
            displayProduct(it)
        } ?: run {
            Toast.makeText(requireContext(), "Producto no encontrado", Toast.LENGTH_SHORT).show()
            goBack()
        }
    }

    private fun displayProduct(product: Product) {
        textName.text = product.name
        textPrice.text = String.format("$%.2f", product.price)
        textDescription.text = product.description
        textStock.text = product.stock.toString()

        // Load product image
        if (product.imageBase64 != null) {
            val bitmap = ImageHelper.base64ToBitmap(product.imageBase64)
            imageMain.setImageBitmap(bitmap)
        } else {
            imageMain.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun setupButtons() {
        buttonBack.setOnClickListener {
            goBack()
        }

        buttonAddToCart.setOnClickListener {
            product?.let { addToCart(it) }
        }

        buttonEdit.setOnClickListener {
            product?.let { showEditDialog(it) }
        }

        buttonDelete.setOnClickListener {
            product?.let { showDeleteConfirmation(it) }
        }
    }

    private fun setupAdminVisibility() {
        if (authService.isAdmin()) {
            adminButtons.visibility = View.VISIBLE
        } else {
            adminButtons.visibility = View.GONE
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
                        loadProduct(product.id)
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
                        goBack()
                    }
                    .onFailure { error ->
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun goBack() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ProductListFragment())
            .commit()

        // Update bottom navigation selection
        val homeActivity = requireActivity()
        if (homeActivity is com.artesan.artesanapp.activities.HomeActivity) {
            homeActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigation
            )?.selectedItemId = R.id.nav_products
        }
    }

    companion object {
        fun newInstance(productId: String): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            val args = Bundle()
            args.putString("productId", productId)
            fragment.arguments = args
            return fragment
        }
    }
}
