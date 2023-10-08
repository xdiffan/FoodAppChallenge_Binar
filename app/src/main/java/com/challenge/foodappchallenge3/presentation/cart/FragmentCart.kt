package com.challenge.foodappchallenge3.presentation.cart

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.challenge.foodappchallenge3.R
import com.challenge.foodappchallenge3.data.local.database.AppDatabase
import com.challenge.foodappchallenge3.data.local.database.datasource.CartDataSource
import com.challenge.foodappchallenge3.data.local.database.datasource.CartDatabaseDataSource
import com.challenge.foodappchallenge3.data.repository.CartRepository
import com.challenge.foodappchallenge3.data.repository.CartRepositoryImpl
import com.challenge.foodappchallenge3.databinding.FragmentCartBinding
import com.challenge.foodappchallenge3.model.Cart
import com.challenge.foodappchallenge3.model.CartMenu
import com.challenge.foodappchallenge3.presentation.checkout.CheckoutActivity
import com.challenge.foodappchallenge3.utils.GenericViewModelFactory
import com.challenge.foodappchallenge3.utils.proceedWhen
import com.challenge.foodappchallenge3.utils.toCurrencyFormat


class FragmentCart : Fragment() {

    private lateinit var binding: FragmentCartBinding

    private val cartListAdapter: CartListAdapter by lazy {
        CartListAdapter(object : CartListener {
            override fun onCartClicked(item: CartMenu) {

            }

            override fun onPlusTotalItemCartClicked(
                cart: Cart,
            ) {
                viewModel.increaseCart(cart)
            }

            override fun onMinusTotalItemCartClicked(
                cart: Cart,
            ) {
                viewModel.decreaseCart(cart)
            }

            override fun onRemoveCartClicked(cart: Cart) {
                viewModel.deleteCart(cart)
            }

            override fun onUserDoneEditingNotes(
                cart: Cart,
            ) {
                viewModel.updateNotes(cart)
            }
        })
    }

    private val viewModel: CartViewModel by viewModels {
        val database =
            AppDatabase.getInstance(requireContext())
        val cartDao = database.cartDao()
        val cartDataSource: CartDataSource =
            CartDatabaseDataSource(cartDao)
        val repo: CartRepository =
            CartRepositoryImpl(cartDataSource)
        GenericViewModelFactory.create(
            CartViewModel(repo)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCartBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        setupCartList()
        observeData()
        setClickListener()
    }

    private fun setClickListener() {
        binding.btnCheckout.setOnClickListener {
            context?.startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
    }

    private fun observeData() {
        viewModel.cartList.observe(
            viewLifecycleOwner
        ) {
            it.proceedWhen(
                doOnSuccess = { result ->
                    binding.layoutState.root.isVisible =
                        false
                    binding.layoutState.pbLoading.isVisible =
                        false
                    binding.layoutState.tvError.isVisible =
                        false
                    binding.rvCart.isVisible =
                        true
                    result.payload?.let { (carts, totalPrice) ->
                        cartListAdapter.submitData(
                            carts
                        )
                        binding.tvTotalPriceAmount.text = totalPrice.toCurrencyFormat()
                    }
                },
                doOnLoading = {
                    binding.layoutState.root.isVisible =
                        true
                    binding.layoutState.pbLoading.isVisible =
                        true
                    binding.layoutState.tvError.isVisible =
                        false
                    binding.rvCart.isVisible =
                        false
                },
                doOnEmpty = { result ->
                    binding.layoutState.root.isVisible =
                        true
                    binding.rvCart.isVisible =
                        false
                    binding.layoutState.pbLoading.isVisible =
                        false
                    binding.layoutState.tvError.isVisible =
                        true
                    binding.layoutState.tvError.text =
                        getString(R.string.error_empty)
                    result.payload?.let { (_, totalPrice) ->
                        binding.tvTotalPriceAmount.text = totalPrice.toCurrencyFormat()

                    }
                },
                doOnError = { err ->
                    binding.layoutState.root.isVisible =
                        true
                    binding.layoutState.pbLoading.isVisible =
                        false
                    binding.layoutState.tvError.isVisible =
                        true
                    binding.layoutState.tvError.text =
                        err.exception?.message.orEmpty()
                    binding.rvCart.isVisible =
                        false
                }
            )
        }
    }

    private fun setupCartList() {
        binding.rvCart.adapter = cartListAdapter
    }
}