package com.example.doancuoikymobile.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.repository.Status
import com.example.doancuoikymobile.viewmodel.ProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide // Khuyên dùng Glide để load ảnh từ URL

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private val viewModel: ProfileViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private lateinit var imgAvatar: CircleImageView
    private lateinit var edtName: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnChangeAvatar: ImageButton
    private lateinit var edtNewPass: TextInputEditText
    private lateinit var edtConfirmPass: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeViewModel()

        // Tải dữ liệu người dùng hiện tại
        viewModel.loadUser()

        btnChangeAvatar.setOnClickListener { pickImage.launch("image/*") }

        btnSave.setOnClickListener {
            val name = edtName.text.toString()
            val newPass = edtNewPass.text.toString()
            val confirmPass = edtConfirmPass.text.toString()

            if (name.isBlank()) {
                edtName.error = "Tên không được để trống"
                return@setOnClickListener
            }

            if (newPass.isNotEmpty()) {
                if (newPass.length < 6) {
                    Toast.makeText(context, "Mật khẩu phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (newPass != confirmPass) {
                    Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            viewModel.updateFullProfile(
                displayName = name,
                imageUri = selectedImageUri,
                newPassword = newPass.ifBlank { null }
            )
        }
    }

    private fun observeViewModel() {
        // 1. Theo dõi dữ liệu User để hiển thị lên Form
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                user?.let {
                    if (selectedImageUri == null) { // Chỉ load ảnh mạng nếu chưa chọn ảnh mới
                        Glide.with(this@EditProfileFragment)
                            .load(it.avatarUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(imgAvatar)
                    }
                    if (edtName.text.isNullOrEmpty()) {
                        edtName.setText(it.displayName)
                    }
                }
            }
        }

        // 2. Theo dõi trạng thái cập nhật
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateStatus.collect { resource ->
                // Kiểm tra null vì updateStatus khởi tạo là null
                resource?.let {
                    when (it.status) {
                        Status.LOADING -> {
                            btnSave.isEnabled = false
                            btnSave.text = "Đang lưu..."
                        }

                        Status.SUCCESS -> {
                            btnSave.isEnabled = true
                            btnSave.text = "Lưu"
                            Toast.makeText(context, it.data ?: "Thành công", Toast.LENGTH_SHORT).show()
                            viewModel.resetUpdateStatus() // Quan trọng: Reset để không hiện lại Toast
                            parentFragmentManager.popBackStack()
                        }

                        Status.ERROR -> {
                            btnSave.isEnabled = true
                            btnSave.text = "Lưu"
                            Toast.makeText(context, it.message ?: "Lỗi", Toast.LENGTH_SHORT).show()
                            viewModel.resetUpdateStatus()
                        }
                    }
                }
            }
        }
    }

    private fun initViews(view: View) {
        imgAvatar = view.findViewById(R.id.imgAvatar)
        edtName = view.findViewById(R.id.edtName)
        btnSave = view.findViewById(R.id.btnSave)
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar)
        edtNewPass = view.findViewById(R.id.edtNewPassword)
        edtConfirmPass = view.findViewById(R.id.edtConfirmPassword)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            imgAvatar.setImageURI(it)
        }
    }
}