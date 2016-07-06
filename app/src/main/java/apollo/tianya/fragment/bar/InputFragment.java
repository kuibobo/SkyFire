package apollo.tianya.fragment.bar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;

import apollo.tianya.R;
import apollo.tianya.emotion.EmotionAdapter;

/**
 * Created by kuibo on 2016/6/27.
 */
public class InputFragment extends BarBaseFragment implements View.OnClickListener {

    private OnActionClickListener mActionListener;
    private AppCompatEditText mEditor;
    private GridView mGridView;
    private EmotionAdapter mEmoAdapter;

    protected int getLayoutId() {
        return R.layout.fragment_detail_input_bar;
    }

    @Override
    public void initView(View view) {
        mEmoAdapter = new EmotionAdapter(super.getContext());
        mGridView = (GridView) view.findViewById(R.id.face_view);
        mGridView.setAdapter(mEmoAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String emo = null;

                emo = mEmoAdapter.getName(position);
                if (emo != null) {
                    int start = 0;
                    SpannableStringBuilder spannable = null;
                    Bitmap bmp = null;

                    start = mEditor.getSelectionStart();
                    spannable = new SpannableStringBuilder(emo);
                    bmp = (Bitmap) mEmoAdapter.getItem(position);
                    if (bmp != null) {
                        BitmapDrawable draw = null;

                        draw = new BitmapDrawable(bmp);
                        draw.setBounds(0, 0, 1 + bmp.getWidth(), bmp.getHeight());
                        draw.setGravity(3);
                        spannable.setSpan(new ImageSpan(draw, 0), 0, spannable.length(), Spannable.SPAN_POINT_MARK);
                        mEditor.getText().insert(start, spannable);
                    }
                }
            }
        });

        mEditor = (AppCompatEditText) view.findViewById(R.id.editor);
        view.findViewById(R.id.btn_change).setOnClickListener(this);
        ((CheckBox)view.findViewById(R.id.btn_face_change)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    hideSoftKeyboard();
                    showEmojiKeyBoard();
                } else {
                    hideEmojiKeyBoard();
                    showSoftKeyboard();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Action action = null;
        if (id == R.id.btn_change)
            action = Action.ACTION_CHANGE;

        if (action != null && mActionListener != null) {
            mActionListener.onActionClick(action);
        }
    }

    public void setOnActionClickListener(OnActionClickListener lis) {
        mActionListener = lis;
    }

    /**
     * 隐藏Emoji并显示软键盘
     */
    public void hideEmojiKeyBoard() {
        mGridView.setVisibility(View.GONE);
    }

    /**
     * 显示Emoji并隐藏软键盘
     */
    public void showEmojiKeyBoard() {
        mGridView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftKeyboard() {
        ((InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                mEditor.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    /**
     * 显示软键盘
     */
    public void showSoftKeyboard() {
        mEditor.requestFocus();
        ((InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE)).showSoftInput(mEditor,
                InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}