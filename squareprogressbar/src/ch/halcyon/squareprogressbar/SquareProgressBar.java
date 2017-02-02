package ch.halcyon.squareprogressbar;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ch.halcyon.squareprogressbar.utils.CalculationUtil;
import ch.halcyon.squareprogressbar.utils.PercentStyle;

public class SquareProgressBar extends RelativeLayout {

    private final SquareProgressView bar;
    private TextView seasonNumber;

    public SquareProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.progressbarview, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar1);
        seasonNumber = (TextView) findViewById(R.id.season_number);
        bar.bringToFront();
    }

    public SquareProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.progressbarview, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar1);
        seasonNumber = (TextView) findViewById(R.id.season_number);
        bar.bringToFront();
    }

    public SquareProgressBar(Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.progressbarview, this, true);
        bar = (SquareProgressView) findViewById(R.id.squareProgressBar1);
        seasonNumber = (TextView) findViewById(R.id.season_number);
        bar.bringToFront();
    }

    public void setText(String text) {
        seasonNumber.setText(text);
    }

    public void setProgress(double progress) {
        bar.setProgress(progress);
    }

    public void setColor(String colorString) {
        bar.setColor(Color.parseColor(colorString));
    }

    public void setWidth(int width) {
        int padding = CalculationUtil.convertDpToPx(width, getContext());
        bar.setWidthInDp(width);
    }

    /**
     * Draws an outline of the progressbar. Looks quite cool in some situations.
     *
     * @param drawOutline true if it should or not.
     * @since 1.3.0
     */
    public void drawOutline(boolean drawOutline) {
        bar.setOutline(drawOutline);
    }

    /**
     * If outline is enabled or not.
     *
     * @return true if outline is enabled.
     */
    public boolean isOutline() {
        return bar.isOutline();
    }

    /**
     * Draws the startline. this is the line where the progressbar starts the
     * drawing around the image.
     *
     * @param drawStartline true if it should or not.
     * @since 1.3.0
     */
    public void drawStartline(boolean drawStartline) {
        bar.setStartline(drawStartline);
    }

    /**
     * If the startline is enabled.
     *
     * @return true if startline is enabled or not.
     */
    public boolean isStartline() {
        return bar.isStartline();
    }

    /**
     * Defines if the percent text should be shown or not. To modify the text
     * checkout {@link #setPercentStyle(ch.halcyon.squareprogressbar.utils.PercentStyle)}.
     *
     * @param showProgress true if it should or not.
     * @since 1.3.0
     */
    public void showProgress(boolean showProgress) {
        bar.setShowProgress(showProgress);
    }

    /**
     * If the progress text inside of the image is enabled.
     *
     * @return true if it is or not.
     */
    public boolean isShowProgress() {
        return bar.isShowProgress();
    }

    /**
     * Returns the {@link PercentStyle} of the percent text. Maybe returns the
     * default value, check {@link #setPercentStyle(PercentStyle)} fo that.
     *
     * @return the percent style of the moment.
     */
    public PercentStyle getPercentStyle() {
        return bar.getPercentStyle();
    }

    /**
     * Sets a custom percent style to the text inside the image. Make sure you
     * set {@link #showProgress(boolean)} to true. Otherwise it doesn't shows.
     * The default settings are:</br>
     * <table>
     * <tr>
     * <th>Text align</td>
     * <td>CENTER</td>
     * </tr>
     * <tr>
     * <th>Text size</td>
     * <td>150 [dp]</td>
     * </tr>
     * <tr>
     * <th>Display percentsign</td>
     * <td>true</td>
     * </tr>
     * <tr>
     * <th>Custom text</td>
     * <td>%</td>
     * </tr>
     * </table>
     *
     * @param percentStyle
     */
    public void setPercentStyle(PercentStyle percentStyle) {
        bar.setPercentStyle(percentStyle);
    }

    /**
     * If the progressbar disappears when the progress reaches 100%.
     *
     * @since 1.4.0
     */
    public boolean isClearOnHundred() {
        return bar.isClearOnHundred();
    }

    public void setClearOnHundred(boolean clearOnHundred) {
        bar.setClearOnHundred(clearOnHundred);
    }


    /**
     * Set an image resource directly to the ImageView.
     *
     * @param bitmap the {@link android.graphics.Bitmap} to set.
     */
/*
    public void setImageBitmap(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }
*/

    /**
     * Returns the status of the indeterminate mode. The default status is false.
     *
     * @since 1.6.0
     */
    public boolean isIndeterminate() {
        return bar.isIndeterminate();
    }

    /**
     * Set the status of the indeterminate mode. The default is false. You can
     * still configure colour, width and so on.
     *
     * @param indeterminate true to enable the indeterminate mode (default true)
     * @since 1.6.0
     */
    public void setIndeterminate(boolean indeterminate) {
        bar.setIndeterminate(indeterminate);
    }

    /**
     * Draws a line in the center of the way the progressbar has to go.
     *
     * @param drawCenterline true if it should or not.
     * @since 1.6.0
     */
    public void drawCenterline(boolean drawCenterline) {
        bar.setCenterline(drawCenterline);
    }

    /**
     * If the centerline is enabled or not.
     *
     * @return true if centerline is enabled.
     * @since 1.6.0
     */
    public boolean isCenterline() {
        return bar.isCenterline();
    }
}
