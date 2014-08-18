package com.nineyi.creditcardbase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nineyi.creditcardbase.CreditCardUtil.CardType;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;

public class CreditCardNumberView extends CreditEntryFieldBase {
	public static final int CC_LEN_FOR_TYPE = 4;
	public static final String REGX_VISA_TYPE = "^4[0-9]{3}?";// VISA 16
	public static final String REGX_MC_TYPE = "^5[1-5][0-9]{2}$";// MC 16
	public interface CardNumberListener{
		public void onCardChanged(CardType type);
		public void onCreditCardNumberValid();
	}	

	
	protected CardType mCardType;
	protected CardNumberListener mCardListener;
	protected String previousNumber;
	public CreditCardNumberView(Context context) {
		super(context);
	}

	public CreditCardNumberView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setCardNumberListener(CardNumberListener listener){
		mCardListener = listener;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	public void init() {		
		super.init();
		setGravity(Gravity.LEFT);
		setHint("5123 4567 8901 2346");		
	}
	
	
	@Override
	public void afterTextChanged(Editable edit) {
		String number = edit.toString();
		if(number.length()>=CC_LEN_FOR_TYPE){
			CardType type = findCardType(number);
			if(type.equals(CardType.INVALID)){
				this.removeTextChangedListener(this);
				this.setText(previousNumber);
				this.setSelection(3);
				this.addTextChangedListener(this);
				if(delegate!=null){
					delegate.onBadInput(this);
				}
				setValid(false);
				return;						
			}else{
				if(mCardType != type){
					if(mCardListener!=null){
						mCardListener.onCardChanged(type);
					}
				}
				
				mCardType = type;
				
				String formatted = CreditCardUtil.formatForViewing(number, type);
				Log.d("Ted", "formate "+number.equalsIgnoreCase(formatted)+" ");
				if (!number.equalsIgnoreCase(formatted)) {
					Log.i("CreditCardText", formatted);
					this.removeTextChangedListener(this);
					this.setText(formatted);
					this.setSelection(formatted.length());
					this.addTextChangedListener(this);
				}				

				if (formatted.length() >= CreditCardUtil
						.lengthOfFormattedStringForType(type)) {
					if (CreditCardUtil.isValidNumber(formatted)) {
						if(mCardListener!=null){
							mCardListener.onCreditCardNumberValid();
						}
						setValid(true);
					} else {
						if(delegate!=null){
							delegate.onBadInput(this);
						}
						setValid(false);
					}
				}
			}
		}else{
			if (mCardType != null) {
				mCardType = null;
				if(mCardListener!=null){
					mCardListener.onCardChanged(CardType.INVALID);
				}
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int arg1, int arg2,
			int arg3) {
		previousNumber = s.toString();
	}

	@Override
	public String helperText() {	
		return null;
	}
	
	public CardType getType() {
		return mCardType;
	}

	public CardType findCardType(String number) {

		if (number.length() < CC_LEN_FOR_TYPE) {
			return CardType.INVALID;
		}
		
		String reg = null;

		for (CardType type : CardType.values()) {
			switch (type) {
			case MASTERCARD:
				reg = REGX_MC_TYPE;
				break;
			case VISA:
				reg = REGX_VISA_TYPE;
				break;
			default:
				break;
			}

			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(number.substring(0,
					CC_LEN_FOR_TYPE));

			if (matcher.matches()) {
				return type;
			}
		}

		return CardType.INVALID;
	}
}
