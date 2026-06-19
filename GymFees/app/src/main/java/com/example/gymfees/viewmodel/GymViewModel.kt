package com.example.gymfees.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.gymfees.data.Customer
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.data.Payment
import com.example.gymfees.repository.GymRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GymRepository
    
    private val _searchQuery = MutableLiveData("")
    private val _filterStatus = MutableLiveData("ALL") // ALL, PAID, DUE_SOON, OVERDUE

    val filteredCustomers: MediatorLiveData<List<Customer>> = MediatorLiveData()
    val customerCount: LiveData<Int>
    val paidCount: LiveData<Int>
    val dueSoonCount: LiveData<Int>
    val overdueCount: LiveData<Int>
    val totalCollection: LiveData<Double>

    // For Snackbar messages
    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> get() = _snackbarMessage

    private val _showUndoAction = MutableLiveData<Pair<Customer, Payment?>>(null)
    val showUndoAction: LiveData<Pair<Customer, Payment?>> get() = _showUndoAction


    init {
        val database = GymDatabase.getDatabase(application)
        repository = GymRepository(database.customerDao(), database.paymentDao())
        
        val today = Calendar.getInstance().timeInMillis
        
        // Start of current month for collection calculation
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfMonth = calendar.timeInMillis

        customerCount = repository.customerCount
        paidCount = database.customerDao().getPaidCount(today)
        dueSoonCount = database.customerDao().getDueSoonCount(today)
        overdueCount = database.customerDao().getOverdueCount(today)
        totalCollection = repository.getTotalCollectionForMonth(startOfMonth)

        filteredCustomers.addSource(repository.allCustomers) { customers ->
            combine(customers, _searchQuery.value ?: "", _filterStatus.value ?: "ALL")
        }
        filteredCustomers.addSource(_searchQuery) { query ->
            combine(repository.allCustomers.value ?: emptyList(), query, _filterStatus.value ?: "ALL")
        }
        filteredCustomers.addSource(_filterStatus) { status ->
            combine(repository.allCustomers.value ?: emptyList(), _searchQuery.value ?: "", status)
        }
    }

    private fun combine(customers: List<Customer>, query: String, status: String) {
        filteredCustomers.value = filterAndSearch(customers, query, status)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: String) {
        _filterStatus.value = status
    }

    private fun filterAndSearch(customers: List<Customer>, query: String, status: String): List<Customer> {
        val today = Calendar.getInstance().timeInMillis
        val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
        
        return customers.filter { customer ->
            val matchesSearch = customer.name.contains(query, ignoreCase = true) || 
                               customer.mobileNumber.contains(query)
            
            val matchesFilter = when (status) {
                "ALL" -> true
                "PAID" -> customer.nextDueDate > today + threeDaysMillis
                "DUE_SOON" -> customer.nextDueDate >= today && customer.nextDueDate <= today + threeDaysMillis
                "OVERDUE" -> customer.nextDueDate < today
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    fun insertCustomer(name: String, mobile: String, joiningDate: Long, fee: Double, notes: String?) = viewModelScope.launch(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = joiningDate
        calendar.add(Calendar.MONTH, 1)
        val nextDueDate = calendar.timeInMillis
        
        val customer = Customer(
            name = name,
            mobileNumber = mobile,
            joiningDate = joiningDate,
            monthlyFee = fee,
            nextDueDate = nextDueDate,
            notes = notes,
            status = "PENDING" // Default to PENDING for new customers
        )
        repository.insertCustomer(customer)
    }

    fun updateCustomer(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateCustomer(customer)
    }

    fun deleteCustomer(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        // Also delete associated payments
        repository.deletePaymentsByCustomerId(customer.id)
        repository.deleteCustomer(customer)
    }

    fun markAsPaid(customer: Customer) = viewModelScope.launch(Dispatchers.IO) {
        if (customer.status == "PAID") {
            _snackbarMessage.postValue("Payment already marked as paid.")
            return@launch
        }

        val paymentDate = System.currentTimeMillis()
        val payment = Payment(
            customerId = customer.id,
            customerName = customer.name,
            amountPaid = customer.monthlyFee,
            paymentDate = paymentDate,
            dueDate = customer.nextDueDate // Store the due date at the time of payment
        )

        repository.insertPayment(payment)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = customer.nextDueDate
        calendar.add(Calendar.MONTH, 1)
        val newNextDueDate = calendar.timeInMillis

        val updatedCustomer = customer.copy(
            nextDueDate = newNextDueDate,
            status = "PAID"
        )
        repository.updateCustomer(updatedCustomer)

        _snackbarMessage.postValue("Payment marked as PAID.")
        _showUndoAction.postValue(Pair(updatedCustomer, payment)) // For undo functionality
    }

    fun markAsUnpaid(customer: Customer, paymentToRevert: Payment?) = viewModelScope.launch(Dispatchers.IO) {
        // If paymentToRevert is null, fetch it first
        val payment = paymentToRevert ?: repository.getLatestPaymentForCustomer(customer.id)

        if (customer.status != "PAID" || payment == null) {
            _snackbarMessage.postValue("Cannot revert payment. Customer status is not PAID or no payment found.")
            return@launch
        }

        // Restore the previous due date from the payment record
        val previousNextDueDate = payment.dueDate

        repository.deletePayment(payment) // Delete the specific payment record

        val updatedCustomer = customer.copy(
            nextDueDate = previousNextDueDate,
            status = "PENDING" // Change status back to PENDING
        )
        repository.updateCustomer(updatedCustomer)

        _snackbarMessage.postValue("Payment has been reversed.")
        _showUndoAction.postValue(Pair(updatedCustomer, null)) // No undo for reversal
    }

    fun getPaymentsForCustomer(customerId: Long): LiveData<List<Payment>> {
        return repository.getPaymentsForCustomer(customerId)
    }

    // Method to clear the Snackbar message
    fun snackbarMessageShown() {
        _snackbarMessage.value = null
    }

    // Method to clear the undo action
    fun undoActionShown() {
        _showUndoAction.value = null
    }
}
