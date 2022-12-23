# 日历与时间选择器
![QQ截图20221223102932](https://user-images.githubusercontent.com/13359093/209259145-42cbcdbf-48d8-4895-8453-cd00a59b68a8.png)
![QQ截图20221223102940](https://user-images.githubusercontent.com/13359093/209259148-f558befd-71f3-4fbb-9cd3-0f8d7a412eda.png)


```
dependencies{
implementation 'com.github.MirShu:DatepickerMirShu:1.2.010'
}

```


```
   <com.humannote.me.views.DatePicker
                android:id="@+id/dp_date"
                android:layout_width="match_parent"
                android:layout_height="wrap_content" />
```

```
   //展示选择今天
   this.dp_date.isSelectToday(true);
   
   //设置是否显示农历
   this.dp_date.isLunarDisplay(true);
   
   //设置选中的日期
    this.dp_date.setSelectDate(new Date());
        this.dp_date.setOnDateSelected(date -> {
            String strDate = date.get(0);
            view_almanac.setAlmanac(strDate);
            this.view_select_date.setSelectDate(DateHelper.stringToDate(strDate));
        });
        
    //点击监听
     this.dp_date.setOnTitleClick(() -> {
            Date selectDate = this.dp_date.getSelectDate();
            new SlideDateTimePicker.Builder(getSupportFragmentManager()).setListener(new SlideDateTimeListener() {
                @Override
                public void onDateTimeSet(Date date) {
                    dp_date.initDatepicker(date);
                    view_almanac.setAlmanac(DateHelper.getYYYYMMDD(date));
                    view_select_date.setSelectDate(date);
                }
            }).setInitialDate(selectDate).build().show();
        });
        
    //手动输入设置记录日期刷新
    this.dp_date.initDatepicker(remindDate);
    
    //获取日期
         Date selectDate = this.dp_date.getSelectDate();
        this.view_almanac.setAlmanac(DateHelper.getYYYYMMDD(selectDate));
```

#### 日期类

```
public class CustomDatePicker extends DatePicker
{
    private static final String TAG = "CustomDatePicker";

    public CustomDatePicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Class<?> idClass = null;
        Class<?> numberPickerClass = null;
        Field selectionDividerField = null;
        Field monthField = null;
        Field dayField = null;
        Field yearField = null;
        NumberPicker monthNumberPicker = null;
        NumberPicker dayNumberPicker = null;
        NumberPicker yearNumberPicker = null;

        try
        {
            // Create an instance of the id class
            idClass = Class.forName("com.android.internal.R$id");

            // Get the fields that store the resource IDs for the month, day and year NumberPickers
            monthField = idClass.getField("month");
            dayField = idClass.getField("day");
            yearField = idClass.getField("year");

            // Use the resource IDs to get references to the month, day and year NumberPickers
            monthNumberPicker = (NumberPicker) findViewById(monthField.getInt(null));
            dayNumberPicker = (NumberPicker) findViewById(dayField.getInt(null));
            yearNumberPicker = (NumberPicker) findViewById(yearField.getInt(null));

            numberPickerClass = Class.forName("android.widget.NumberPicker");

            // Set the value of the mSelectionDivider field in the month, day and year NumberPickers
            // to refer to our custom drawables
            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(monthNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(dayNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(yearNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
        }
        catch (ClassNotFoundException e)
        {
            Log.e(TAG, "ClassNotFoundException in CustomDatePicker", e);
        }
        catch (NoSuchFieldException e)
        {
            Log.e(TAG, "NoSuchFieldException in CustomDatePicker", e);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "IllegalAccessException in CustomDatePicker", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "IllegalArgumentException in CustomDatePicker", e);
        }
    }
}

```

#### 时间类

```
public class CustomTimePicker extends TimePicker
{
    private static final String TAG = "CustomTimePicker";

    public CustomTimePicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Class<?> idClass = null;
        Class<?> numberPickerClass = null;
        Field selectionDividerField = null;
        Field hourField = null;
        Field minuteField = null;
        Field amPmField = null;
        NumberPicker hourNumberPicker = null;
        NumberPicker minuteNumberPicker = null;
        NumberPicker amPmNumberPicker = null;

        try
        {
            // Create an instance of the id class
            idClass = Class.forName("com.android.internal.R$id");

            // Get the fields that store the resource IDs for the hour, minute and amPm NumberPickers
            hourField = idClass.getField("hour");
            minuteField = idClass.getField("minute");
            amPmField = idClass.getField("amPm");

            // Use the resource IDs to get references to the hour, minute and amPm NumberPickers
            hourNumberPicker = (NumberPicker) findViewById(hourField.getInt(null));
            minuteNumberPicker = (NumberPicker) findViewById(minuteField.getInt(null));
            amPmNumberPicker = (NumberPicker) findViewById(amPmField.getInt(null));

            numberPickerClass = Class.forName("android.widget.NumberPicker");

            // Set the value of the mSelectionDivider field in the hour, minute and amPm NumberPickers
            // to refer to our custom drawables
            selectionDividerField = numberPickerClass.getDeclaredField("mSelectionDivider");
            selectionDividerField.setAccessible(true);
            selectionDividerField.set(hourNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(minuteNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
            selectionDividerField.set(amPmNumberPicker, getResources().getDrawable(R.drawable.selection_divider));
        }
        catch (ClassNotFoundException e)
        {
            Log.e(TAG, "ClassNotFoundException in CustomTimePicker", e);
        }
        catch (NoSuchFieldException e)
        {
            Log.e(TAG, "NoSuchFieldException in CustomTimePicker", e);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "IllegalAccessException in CustomTimePicker", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "IllegalArgumentException in CustomTimePicker", e);
        }
    }
}

```

### 展示日期 年月日时 Fragment

```
public class DateFragment extends Fragment
{
    /**
     * Used to communicate back to the parent fragment as the user
     * is changing the date spinners so we can dynamically update
     * the tab text.
     */
    public interface DateChangedListener
    {
        void onDateChanged(int year, int month, int day);
    }

    private DateChangedListener mCallback;
    private CustomDatePicker mDatePicker;

    public DateFragment()
    {
        // Required empty public constructor for fragment.
    }

    /**
     * Cast the reference to {@link SlideDateTimeDialogFragment}
     * to a {@link DateChangedListener}.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            mCallback = (DateChangedListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("Calling fragment must implement " +
                "DateFragment.DateChangedListener interface");
        }
    }

    /**
     * Return an instance of DateFragment with its bundle filled with the
     * constructor arguments. The values in the bundle are retrieved in
     *
     * @param theme
     * @param year
     * @param month
     * @param day
     * @param minDate
     * @param maxDate
     * @return an instance of DateFragment
     */
    public static final DateFragment newInstance(int theme, int year, int month,
            int day, Date minDate, Date maxDate)
    {
        DateFragment f = new DateFragment();

        Bundle b = new Bundle();
        b.putInt("theme", theme);
        b.putInt("year", year);
        b.putInt("month", month);
        b.putInt("day", day);
        b.putSerializable("minDate", minDate);
        b.putSerializable("maxDate", maxDate);
        f.setArguments(b);

        return f;
    }

    /**
     * Create and return the user interface view for this fragment.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        int theme = getArguments().getInt("theme");
        int initialYear = getArguments().getInt("year");
        int initialMonth = getArguments().getInt("month");
        int initialDay = getArguments().getInt("day");
        Date minDate = (Date) getArguments().getSerializable("minDate");
        Date maxDate = (Date) getArguments().getSerializable("maxDate");

        // Unless we inflate using a cloned inflater with a Holo theme,
        // on Lollipop devices the DatePicker will be the new-style
        // DatePicker, which is not what we want. So we will
        // clone the inflater that we're given but with our specified
        // theme, then inflate the layout with this new inflater.

        Context contextThemeWrapper = new ContextThemeWrapper(
                getActivity(),
                theme == SlideDateTimePicker.HOLO_DARK ?
                         android.R.style.Theme_Holo :
                         android.R.style.Theme_Holo_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        View v = localInflater.inflate(R.layout.fragment_date, container, false);

        mDatePicker = (CustomDatePicker) v.findViewById(R.id.datePicker);
        // block keyboard popping up on touch
        mDatePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        mDatePicker.init(
                initialYear,
                initialMonth,
                initialDay,
                new OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                        mCallback.onDateChanged(year, monthOfYear, dayOfMonth);
                    }
                });

        if (minDate != null)
            mDatePicker.setMinDate(minDate.getTime());

        if (maxDate != null)
            mDatePicker.setMaxDate(maxDate.getTime());

        return v;
    }
}

```
