package com.nineyi.creditcardbase;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;

public class ExpDateText extends CreditEntryFieldBase {

	String previousString;
	ExpDateListener mListener;
	public interface ExpDateListener{
		public void onExpirationDateValid();
	}
	
	
	public ExpDateText(Context context) {
		super(context);
		init();
	}

	public ExpDateText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ExpDateText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setOnExpDateListener(ExpDateListener listener){
		mListener = listener;
	}

	public void init() {
		super.init();
		setHint("MM/YY");
	}

	/* TextWatcher Implementation Methods */
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		previousString = s.toString();
	}

	public void afterTextChanged(Editable s) {
		String updatedString = s.toString();

		// if delete occured do not format
		if (updatedString.length() > previousString.length()) {
			this.removeTextChangedListener(this);
			String formatted = CreditCardUtil
					.formatExpirationDate(s.toString());
			Log.i("CreditCardText", formatted);
			this.setText(formatted);
			this.setSelection(formatted.length());
			this.addTextChangedListener(this);
			
			if(formatted.length() == 5){
				if(mListener!=null){
					mListener.onExpirationDateValid();
				}
				setValid(true);
			}
			else if(formatted.length() < updatedString.length())
			{
				if(delegate!=null){
					delegate.onBadInput(this);
				}
				setValid(false);
			}
		}
	}

	@Override
	public String helperText() {
		return null;
	}
}
