package com.nineyi.creditcardbase;




import com.example.creditcardlayout.R;
import com.nineyi.creditcardbase.CreditCardNumberView.CardNumberListener;
import com.nineyi.creditcardbase.CreditCardUtil.CardType;
import com.nineyi.creditcardbase.CreditEntryFieldBase.CreditCardFieldDelegate;
import com.nineyi.creditcardbase.ExpDateText.ExpDateListener;
import com.nineyi.creditcardbase.SecurityCodeText.SecurityListener;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;



public class CreditCardView extends LinearLayout implements OnTouchListener, CardNumberListener, ExpDateListener, SecurityListener, CreditCardFieldDelegate  {
	private HorizontalScrollView mScrollview;
	private CreditCardNumberView mNum;
	private TextView mFourDigit;	
	private ExpDateText mExpireDate;
	private SecurityCodeText mSecurity;
	private boolean showingBack;
	private ImageView cardImage;
	private ImageView backCardImage;
	public CreditCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CreditCardView(Context context) {
		super(context);
		init();
	}
	
	

	public void init() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.mylayout, this);
		cardImage = (ImageView)view.findViewById(R.id.img);
		cardImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
	        public void onGlobalLayout() {
	            // Ensure you call it only once :
	        	
	        	if(android.os.Build.VERSION.SDK_INT >= 16){
	        		cardImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	        	}else{
	        		cardImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	        	}
	        	initNumberWidth();
	        }
	    });
		backCardImage = (ImageView)view.findViewById(R.id.imgback);
		mScrollview = (HorizontalScrollView)view.findViewById(R.id.scrollview);		
		mScrollview.setHorizontalScrollBarEnabled(false);
		mScrollview.setOnTouchListener(this);
		mScrollview.setSmoothScrollingEnabled(true);
		
		View horizontalview = inflater.inflate(R.layout.horizontal_layout, null);
		mNum = (CreditCardNumberView)horizontalview.findViewById(R.id.creditcard_num);
		mNum.setCardNumberListener(this);
		
		mFourDigit = (TextView)horizontalview.findViewById(R.id.four_digit);
		mExpireDate = (ExpDateText)horizontalview.findViewById(R.id.expire_date);
		mExpireDate.setOnExpDateListener(this);
		mSecurity = (SecurityCodeText)horizontalview.findViewById(R.id.security_code);		
		mSecurity.setOnSecurityCodeTextListener(this);
		mScrollview.addView(horizontalview);
		
		mNum.setDelegate(this);
		mExpireDate.setDelegate(this);
		mSecurity.setDelegate(this);
		mNum.requestFocus();
	}
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void initNumberWidth() {
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		int width, height;

		if (currentapiVersion < 13) {
			width = display.getWidth(); // deprecated
			height = display.getHeight();
		} else {
			Point size = new Point();
			display.getSize(size);
			width = size.x;
			height = size.y;
		}
		Log.d("Ted", "image "+ cardImage.getWidth());
		width = width - cardImage.getWidth();
		mNum.setWidth(width);
		mNum.invalidate();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {		
		return true;
	}
	
	public void updateCardImage(boolean back) {
		if (showingBack != back) {
			flipCardImage();
		}

		showingBack = back;
	}


	public void flipCardImage() {
		FlipAnimator animator = new FlipAnimator(cardImage, backCardImage,
				backCardImage.getWidth() / 2, backCardImage.getHeight() / 2);
		if (cardImage.getVisibility() == View.GONE) {
			animator.reverse();
		}
		cardImage.startAnimation(animator);
	}

	@Override
	public void onCardChanged(CardType type) {
		cardImage.setImageResource(CreditCardUtil.cardImageForCardType(type,
				false));
		backCardImage.setImageResource(CreditCardUtil.cardImageForCardType(
				type, true));
		updateCardImage(false);
	}

	@Override
	public void onCreditCardNumberValid() {		
		Log.d("Ted", "onCreditCardNumberValid");
		focusOnField(mExpireDate);

		String number = mNum.getText().toString();
		int length = number.length();
		String digits = number.substring(length - 4);
		mFourDigit.setText(digits);
		Log.i("CreditCardNumber", number);
	}
	
	@Override
	public void onExpirationDateValid() {
		focusOnField(mSecurity);
	}

	@Override
	public void onSecurityCodeValid() {
		
	}

	public void focusOnField(CreditEntryFieldBase field) {
		field.setFocusableInTouchMode(true);
		field.requestFocus();
		field.setFocusableInTouchMode(false);

		/*if (this.textHelper != null) {
			this.textHelper.setText(field.helperText());
		}*/

		if (field.getClass().equals(CreditCardNumberView.class)) {
			mScrollview.scrollTo(0, 0);
			
		} else {
			mScrollview.scrollTo(mNum.getWidth(),0);
			
		}

		if (field.getClass().equals(SecurityCodeText.class)) {
			((SecurityCodeText) field).setType(mNum.getType());
			updateCardImage(true);
		} else {
			updateCardImage(false);
		}
	}

	@Override
	public void focusOnPreviousField(CreditEntryFieldBase field) {
		if (field.getClass().equals(ExpDateText.class)) {
			focusOnField(mNum);
		} else if (field.getClass().equals(SecurityCodeText.class)) {
			focusOnField(mExpireDate);
		}
	}

	@Override
	public void onBadInput(final CreditEntryFieldBase base) {
		
			Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
			base.startAnimation(shake);
			base.setTextColor(Color.RED);

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					base.setTextColor(Color.BLACK);
				}
			}, 1000);
	}


}
