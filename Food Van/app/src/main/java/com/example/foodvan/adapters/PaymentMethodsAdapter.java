package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.PaymentMethod;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Adapter for displaying payment methods in RecyclerView
 */
public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder> {

    private Context context;
    private List<PaymentMethod> paymentMethods;
    private OnPaymentMethodActionListener listener;

    public interface OnPaymentMethodActionListener {
        void onEditPaymentMethod(PaymentMethod paymentMethod);
        void onDeletePaymentMethod(PaymentMethod paymentMethod);
        void onSetDefaultPaymentMethod(PaymentMethod paymentMethod);
        void onPaymentMethodClicked(PaymentMethod paymentMethod);
    }

    public PaymentMethodsAdapter(Context context, List<PaymentMethod> paymentMethods, OnPaymentMethodActionListener listener) {
        this.context = context;
        this.paymentMethods = paymentMethods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.bind(paymentMethod);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private ImageView ivPaymentIcon;
        private TextView tvPaymentType;
        private TextView tvPaymentDetails;
        private TextView tvPaymentSubtitle;
        private TextView tvDefaultBadge;
        private ImageView ivMoreOptions;
        private LinearLayout layoutActionButtons;
        private LinearLayout layoutStatusIndicators;
        private LinearLayout layoutVerified;
        private LinearLayout layoutExpired;
        private MaterialButton btnEdit;
        private MaterialButton btnSetDefault;
        private MaterialButton btnDelete;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            ivPaymentIcon = itemView.findViewById(R.id.iv_payment_icon);
            tvPaymentType = itemView.findViewById(R.id.tv_payment_type);
            tvPaymentDetails = itemView.findViewById(R.id.tv_payment_details);
            tvPaymentSubtitle = itemView.findViewById(R.id.tv_payment_subtitle);
            tvDefaultBadge = itemView.findViewById(R.id.tv_default_badge);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
            layoutActionButtons = itemView.findViewById(R.id.layout_action_buttons);
            layoutStatusIndicators = itemView.findViewById(R.id.layout_status_indicators);
            layoutVerified = itemView.findViewById(R.id.layout_verified);
            layoutExpired = itemView.findViewById(R.id.layout_expired);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(PaymentMethod paymentMethod) {
            // Set payment type and icon
            tvPaymentType.setText(paymentMethod.getType().getDisplayName());
            setPaymentIcon(paymentMethod);
            
            // Set payment details
            tvPaymentDetails.setText(paymentMethod.getFormattedDetails());
            
            // Set subtitle based on payment type
            setPaymentSubtitle(paymentMethod);
            
            // Show/hide default badge
            tvDefaultBadge.setVisibility(paymentMethod.isDefault() ? View.VISIBLE : View.GONE);
            
            // Setup status indicators
            setupStatusIndicators(paymentMethod);
            
            // Setup click listeners
            setupClickListeners(paymentMethod);
            
            // Initially hide action buttons
            layoutActionButtons.setVisibility(View.GONE);
        }

        private void setPaymentIcon(PaymentMethod paymentMethod) {
            int iconRes;
            switch (paymentMethod.getType()) {
                case CASH:
                    iconRes = R.drawable.ic_money;
                    break;
                case UPI:
                    iconRes = getUpiProviderIcon(paymentMethod.getUpiProvider());
                    break;
                case CREDIT_CARD:
                    iconRes = R.drawable.ic_credit_card;
                    break;
                case DEBIT_CARD:
                    iconRes = R.drawable.ic_debit_card;
                    break;
                case NET_BANKING:
                    iconRes = R.drawable.ic_bank;
                    break;
                case WALLET:
                    iconRes = getWalletProviderIcon(paymentMethod.getWalletProvider());
                    break;
                default:
                    iconRes = R.drawable.ic_payment;
                    break;
            }
            ivPaymentIcon.setImageResource(iconRes);
        }

        private int getUpiProviderIcon(String provider) {
            if (provider == null) return R.drawable.ic_upi;
            
            switch (provider.toLowerCase()) {
                case "google pay":
                case "gpay":
                    return R.drawable.ic_gpay;
                case "phonepe":
                    return R.drawable.ic_phonepe;
                case "paytm":
                    return R.drawable.ic_paytm;
                default:
                    return R.drawable.ic_upi;
            }
        }

        private int getWalletProviderIcon(String provider) {
            if (provider == null) return R.drawable.ic_wallet;
            
            switch (provider.toLowerCase()) {
                case "paytm":
                case "paytm wallet":
                    return R.drawable.ic_paytm;
                case "phonepe":
                case "phonepe wallet":
                    return R.drawable.ic_phonepe;
                case "amazon pay":
                    return R.drawable.ic_amazon_pay;
                default:
                    return R.drawable.ic_wallet;
            }
        }

        private void setPaymentSubtitle(PaymentMethod paymentMethod) {
            String subtitle = "";
            boolean showSubtitle = false;
            
            switch (paymentMethod.getType()) {
                case CREDIT_CARD:
                case DEBIT_CARD:
                    if (paymentMethod.getExpiryDate() != null) {
                        subtitle = "Expires " + paymentMethod.getExpiryDate();
                        showSubtitle = true;
                    }
                    break;
                case UPI:
                    if (paymentMethod.getUpiProvider() != null) {
                        subtitle = paymentMethod.getUpiProvider();
                        showSubtitle = true;
                    }
                    break;
                case WALLET:
                    if (paymentMethod.getWalletProvider() != null) {
                        subtitle = paymentMethod.getWalletProvider();
                        showSubtitle = true;
                    }
                    break;
                case NET_BANKING:
                    if (paymentMethod.getBankName() != null) {
                        subtitle = paymentMethod.getBankName();
                        showSubtitle = true;
                    }
                    break;
            }
            
            tvPaymentSubtitle.setText(subtitle);
            tvPaymentSubtitle.setVisibility(showSubtitle ? View.VISIBLE : View.GONE);
        }

        private void setupStatusIndicators(PaymentMethod paymentMethod) {
            boolean showIndicators = false;
            
            // Show verified status
            if (paymentMethod.isVerified()) {
                layoutVerified.setVisibility(View.VISIBLE);
                showIndicators = true;
            } else {
                layoutVerified.setVisibility(View.GONE);
            }
            
            // Show expired status for cards
            if (paymentMethod.isExpired()) {
                layoutExpired.setVisibility(View.VISIBLE);
                showIndicators = true;
            } else {
                layoutExpired.setVisibility(View.GONE);
            }
            
            layoutStatusIndicators.setVisibility(showIndicators ? View.VISIBLE : View.GONE);
        }

        private void setupClickListeners(PaymentMethod paymentMethod) {
            // Card click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentMethodClicked(paymentMethod);
                }
            });
            
            // More options menu
            ivMoreOptions.setOnClickListener(v -> showOptionsMenu(v, paymentMethod));
            
            // Action buttons
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditPaymentMethod(paymentMethod);
                }
                hideActionButtons();
            });
            
            btnSetDefault.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetDefaultPaymentMethod(paymentMethod);
                }
                hideActionButtons();
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePaymentMethod(paymentMethod);
                }
                hideActionButtons();
            });
        }

        private void showOptionsMenu(View anchor, PaymentMethod paymentMethod) {
            PopupMenu popup = new PopupMenu(context, anchor);
            popup.getMenuInflater().inflate(R.menu.menu_payment_method_options, popup.getMenu());
            
            // Hide "Set as Default" if already default
            if (paymentMethod.isDefault()) {
                popup.getMenu().findItem(R.id.action_set_default).setVisible(false);
            }
            
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditPaymentMethod(paymentMethod);
                    }
                    return true;
                } else if (itemId == R.id.action_set_default) {
                    if (listener != null) {
                        listener.onSetDefaultPaymentMethod(paymentMethod);
                    }
                    return true;
                } else if (itemId == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeletePaymentMethod(paymentMethod);
                    }
                    return true;
                }
                return false;
            });
            
            popup.show();
        }

        private void showActionButtons() {
            layoutActionButtons.setVisibility(View.VISIBLE);
        }

        private void hideActionButtons() {
            layoutActionButtons.setVisibility(View.GONE);
        }
    }
}
