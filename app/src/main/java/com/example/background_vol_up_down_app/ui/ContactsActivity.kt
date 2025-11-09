package com.example.background_vol_up_down_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.background_vol_up_down_app.R
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.local.entities.ContactType
import com.example.background_vol_up_down_app.data.local.entities.TrustedContact
import com.example.background_vol_up_down_app.data.repository.TrustedContactRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ContactsActivity : AppCompatActivity() {

    private lateinit var repository: TrustedContactRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: ContactsAdapter
    private val contacts = mutableListOf<TrustedContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        // Initialize repository
        val database = HerSafeDatabase.getDatabase(this)
        repository = TrustedContactRepository(database.trustedContactDao())

        // Initialize views
        recyclerView = findViewById(R.id.recyclerContacts)
        emptyState = findViewById(R.id.emptyState)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddContact)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = ContactsAdapter(contacts) { contact ->
            showDeleteConfirmDialog(contact)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // FAB click
        fab.setOnClickListener {
            showAddContactDialog()
        }

        // Load contacts
        loadContacts()
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            repository.allActiveContacts.collect { contactsList ->
                contacts.clear()
                contacts.addAll(contactsList)
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    if (contacts.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = dialogView.findViewById<TextInputEditText>(R.id.etPhone)
        val etRelationship = dialogView.findViewById<TextInputEditText>(R.id.etRelationship)
        val switchEmergency = dialogView.findViewById<SwitchMaterial>(R.id.switchEmergencyContact)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btnSave).setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val relationship = etRelationship.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال الاسم", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال رقم الهاتف", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Add contact
            lifecycleScope.launch {
                try {
                    val contact = TrustedContact(
                        name = name,
                        phoneNumber = phone,
                        relationship = relationship.ifEmpty { null },
                        contactType = if (switchEmergency.isChecked) ContactType.EMERGENCY else ContactType.BACKUP,
                        receiveSms = true
                    )
                    repository.insertContact(contact)
                    runOnUiThread {
                        Toast.makeText(this@ContactsActivity, "تم إضافة جهة الاتصال", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ContactsActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmDialog(contact: TrustedContact) {
        AlertDialog.Builder(this)
            .setTitle("حذف جهة الاتصال")
            .setMessage("هل تريد حذف ${contact.name}؟")
            .setPositiveButton("حذف") { _, _ ->
                deleteContact(contact)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun deleteContact(contact: TrustedContact) {
        lifecycleScope.launch {
            try {
                repository.deleteContact(contact)
                runOnUiThread {
                    Toast.makeText(this@ContactsActivity, "تم الحذف", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ContactsActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class ContactsAdapter(
    private val contacts: List<TrustedContact>,
    private val onDeleteClick: (TrustedContact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val tvRelationship: TextView = view.findViewById(R.id.tvRelationship)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phoneNumber

        if (contact.relationship != null) {
            holder.tvRelationship.text = contact.relationship
            holder.tvRelationship.visibility = View.VISIBLE
        } else {
            holder.tvRelationship.visibility = View.GONE
        }

        holder.tvType.text = when (contact.contactType) {
            ContactType.EMERGENCY -> "طوارئ"
            ContactType.SAFE_JOURNEY -> "رحلة آمنة"
            ContactType.BACKUP -> "احتياطي"
            ContactType.AUTHORITY -> "سلطة"
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(contact)
        }
    }

    override fun getItemCount() = contacts.size
}
