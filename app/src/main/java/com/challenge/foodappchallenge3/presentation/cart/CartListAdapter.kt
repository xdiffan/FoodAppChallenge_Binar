package com.challenge.foodappchallenge3.presentation.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.challenge.foodappchallenge3.core.ViewHolderBinder
import com.challenge.foodappchallenge3.databinding.CartItemsBinding
import com.challenge.foodappchallenge3.databinding.CheckoutListItemBinding
import com.challenge.foodappchallenge3.model.Cart
import com.challenge.foodappchallenge3.presentation.checkout.CheckoutViewHolder

class CartListAdapter(
    private val cartListener: CartListener? = null
) : RecyclerView.Adapter<ViewHolder>() {
    private val dataDiffer = AsyncListDiffer(
        this,
        object : DiffUtil.ItemCallback<Cart>() {
            override fun areItemsTheSame(
                oldItem: Cart,
                newItem: Cart
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Cart,
                newItem: Cart
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return if (cartListener != null) {
            CartItemViewHolder(
                CartItemsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                cartListener
            )
        } else {
            CheckoutViewHolder(
                CheckoutListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }
    fun submitData(data: List<Cart>) {
        dataDiffer.submitList(data)
    }
    override fun getItemCount(): Int = dataDiffer.currentList.size
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        (holder as ViewHolderBinder<Cart>).bind(dataDiffer.currentList[position])
    }
}
