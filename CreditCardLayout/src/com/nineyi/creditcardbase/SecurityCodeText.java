package com.nineyi.creditcardbase;

import com.nineyi.creditcardbase.CreditCardUtil.CardType;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.util.AttributeSet;

public class SecurityCodeText extends CreditEntryFieldBase {
	public interface SecurityListener{
		public void onSecurityCodeValid();
	}
	private CardType type;
	
	private int length;
	
	SecurityListener mListener;
	
	public void setOnSecurityCodeTextListener(SecurityListener listener){
		mListener = listener;
	}
	public SecurityCodeText(Context context) {
		super(context);
		init();
	}

	public SecurityCodeText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SecurityCodeText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		super.init();
		setHint("CVV");
	}

	/* TextWatcher Implementation Methods */
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	public void afterTextChanged(Editable s) {

		if (type != null) {
			String number = s.toString();

			if (number.length() == length) {
				if(mListener!=null){
					mListener.onSecurityCodeValid();
				}
				setValid(true);
			}
			else
			{
				setValid(false);
			}
		} else {
			this.removeTextChangedListener(this);
			this.setText("");
			this.addTextChangedListener(this);
		}

	}

	public CardType getType() {
		return type;
	}

	public void setType(CardType type) {
		this.type = type;
		this.length = CreditCardUtil.securityCodeValid(type);
		
		setFilters(new InputFilter[] { new InputFilter.LengthFilter(length) });
	}
	
	@Override
	public String helperText() {
		return null;
	}
}
