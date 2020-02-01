package me.jellysquid.mods.sodium.client.gui.options.control;

import me.jellysquid.mods.sodium.client.gui.options.Option;
import net.minecraft.client.util.Rect2i;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.Validate;

public class SliderControl implements Control<Integer> {
    private final Option<Integer> option;

    private final int min, max, interval;

    private final SliderMode mode;

    public SliderControl(Option<Integer> option, int min, int max, int interval, SliderMode mode) {
        Validate.isTrue(max > min, "The maximum value must be greater than the minimum value");
        Validate.isTrue(interval > 0, "The slider interval must be greater than zero");
        Validate.isTrue(((max - min) % interval) == 0, "The maximum value must be divisable by the interval");
        Validate.notNull(mode, "The slider mode must not be null");

        this.option = option;
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.mode = mode;
    }

    @Override
    public ControlElement<Integer> createElement(Rect2i dim) {
        return new Button(this.option, dim, this.min, this.max, this.interval, this.mode);
    }

    @Override
    public Option<Integer> getOption() {
        return this.option;
    }

    private static class Button extends ControlElement<Integer> {
        private static final int THUMB_WIDTH = 2, TRACK_HEIGHT = 1;

        private final Rect2i sliderBounds;
        private final SliderMode mode;

        private final int min;
        private final int range;
        private final int interval;

        private double thumbPosition;

        public Button(Option<Integer> option, Rect2i dim, int min, int max, int interval, SliderMode mode) {
            super(option, dim);

            this.min = min;
            this.range = max - min;
            this.interval = interval;
            this.thumbPosition = this.getThumbPositionForValue(option.getValue());
            this.mode = mode;

            this.sliderBounds = new Rect2i(dim.getX() + dim.getWidth() - 96, dim.getY() + (dim.getHeight() / 2) - 5, 90, 10);
        }

        @Override
        public void render(int mouseX, int mouseY, float delta) {
            super.render(mouseX, mouseY, delta);

            if (this.option.isAvailable() && this.hovered) {
                this.renderSlider(mouseX, mouseY, delta);
            } else {
                this.renderStandaloneValue(mouseX, mouseY, delta);
            }
        }

        private void renderStandaloneValue(int mouseX, int mouseY, float delta) {
            int sliderX = this.sliderBounds.getX();
            int sliderY = this.sliderBounds.getY();
            int sliderWidth = this.sliderBounds.getWidth();
            int sliderHeight = this.sliderBounds.getHeight();

            String label = this.mode.format(String.valueOf((int) this.option.getValue()));
            int labelWidth = this.font.getStringWidth(label);

            this.drawString(label, sliderX + sliderWidth - labelWidth, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
        }

        private void renderSlider(int mouseX, int mouseY, float delta) {
            int sliderX = this.sliderBounds.getX();
            int sliderY = this.sliderBounds.getY();
            int sliderWidth = this.sliderBounds.getWidth();
            int sliderHeight = this.sliderBounds.getHeight();

            int thumbOffset = (int) Math.floor((double) (this.getIntValue() - this.min) / this.range * sliderWidth);

            int thumbX = sliderX + thumbOffset - THUMB_WIDTH;
            int trackY = sliderY + (sliderHeight / 2) - TRACK_HEIGHT;

            drawRect(thumbX, sliderY, thumbX + (THUMB_WIDTH * 2), sliderY + sliderHeight, 0xFFFFFFFF);
            drawRect(sliderX, trackY, sliderX + sliderWidth, trackY + TRACK_HEIGHT, 0xFFFFFFFF);

            String label = this.mode.format(String.valueOf(this.getIntValue()));

            int labelWidth = this.font.getStringWidth(label);

            this.drawString(label, sliderX - labelWidth - 6, sliderY + (sliderHeight / 2) - 4, 0xFFFFFFFF);
        }

        public int getIntValue() {
            return this.min + (this.interval * (int) Math.round(this.getSnappedThumbPosition() / this.interval));
        }

        public double getSnappedThumbPosition() {
            return this.thumbPosition / (1.0D / this.range);
        }

        public double getThumbPositionForValue(int value) {
            return (value - this.min) * (1.0D / this.range);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.option.isAvailable() && button == 0 && this.sliderBounds.contains((int) mouseX, (int) mouseY)) {
                this.setValueFromMouse(mouseX);

                return true;
            }

            return false;
        }

        private void setValueFromMouse(double d) {
            this.setValue((d - (double) (this.sliderBounds.getX() + 4)) / (double) (this.sliderBounds.getWidth() - 8));
        }

        private void setValue(double d) {
            this.thumbPosition = MathHelper.clamp(d, 0.0D, 1.0D);

            int value = this.getIntValue();

            if (this.option.getValue() != value) {
                this.option.setValue(value);
            }
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.option.isAvailable() && button == 0) {
                this.setValueFromMouse(mouseX);

                return true;
            }

            return false;
        }
    }

    public enum SliderMode {
        PERCENTAGE("%s%%"),
        NUMBER("%s");

        private final String format;

        SliderMode(String format) {
            this.format = format;
        }

        public String format(String... args) {
            return String.format(this.format, (Object[]) args);
        }
    }
}