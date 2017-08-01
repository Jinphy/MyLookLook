/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jinphy.mylooklook.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility methods for working with animations.
 */
public class AnimUtils {

    private AnimUtils() { }

    // interpolator 的类型
    public static final int TYPE_FAST_OUT_SLOW_IN = 0;
    public static final int TYPE_FAST_OUT_LINEAR_IN = 1;
    public static final int TYPE_LINEAR_OUT_SLOW_IN = 2;
    public static final int TYPE_ACCELERATE = 3;
    public static final int TYPE_ACCELERATE_DECELERATE = 4;
    public static final int TYPE_INTICIPATE = 5;
    public static final int TYPE_ANTICIPATE_OVER_SHOOT = 6;
    public static final int TYPE_DECELERATE = 7;
    public static final int TYPE_LINEAR = 8;
    public static final int TYPE_BOUNCE = 9;


    /**
     * 根据类型获取相应的interpolator对象
     *
     * @param context 上下文
     * @param type interpolator的类型，参数值可以参考该类的静态常量
     * @return 生成的interpolator
     * */
    public static Interpolator getInterpolator(Context context,int type){
        switch (type){
            case TYPE_FAST_OUT_SLOW_IN:
                return AnimationUtils.loadInterpolator(context,
                        android.R.interpolator.fast_out_slow_in);
            case TYPE_FAST_OUT_LINEAR_IN:
                return AnimationUtils.loadInterpolator(context,
                        android.R.interpolator.fast_out_linear_in);
            case TYPE_LINEAR_OUT_SLOW_IN:
                return AnimationUtils.loadInterpolator(context,
                        android.R.interpolator.linear_out_slow_in);
            case TYPE_ACCELERATE:
                return new AccelerateInterpolator();
            case TYPE_ACCELERATE_DECELERATE:
                return new AccelerateDecelerateInterpolator();
            case TYPE_INTICIPATE:
                return new AnticipateInterpolator();
            case TYPE_ANTICIPATE_OVER_SHOOT:
                return new AnticipateOvershootInterpolator();
            case TYPE_DECELERATE:
                return new DecelerateInterpolator();
            case TYPE_LINEAR:
                return new LinearInterpolator();
            case TYPE_BOUNCE:
                return new BounceInterpolator();
            default:
                return null;
        }
    }

    /**
     * An implementation of {@link Property} to be used specifically with fields of
     * type
     * <code>float</code>. This type-specific subclass enables performance benefit by allowing
     * calls to a {@link #set(Object, Float) set()} function that takes the primitive
     * <code>float</code> type and avoids autoboxing and other overhead associated with the
     * <code>Float</code> class.
     *
     * @param <T> The class on which the Property is declared.
     **/
    public static abstract class FloatProperty<T> extends Property<T, Float> {
        public FloatProperty(String name) {
            super(Float.class, name);
        }

        /**
         * A type-specific override of the {@link #set(Object, Float)} that is faster when dealing
         * with fields of type <code>float</code>.
         */
        public abstract void setValue(T object, float value);

        @Override
        final public void set(T object, Float value) {
            setValue(object, value);
        }
    }

    /**
     * An implementation of {@link Property} to be used specifically with fields of
     * type
     * <code>int</code>. This type-specific subclass enables performance benefit by allowing
     * calls to a {@link #set(Object, Integer) set()} function that takes the primitive
     * <code>int</code> type and avoids autoboxing and other overhead associated with the
     * <code>Integer</code> class.
     *
     * @param <T> The class on which the Property is declared.
     */
    public static abstract class IntProperty<T> extends Property<T, Integer> {

        public IntProperty(String name) {
            super(Integer.class, name);
        }

        /**
         * A type-specific override of the {@link #set(Object, Integer)} that is faster when dealing
         * with fields of type <code>int</code>.
         */
        public abstract void setValue(T object, int value);

        @Override
        final public void set(T object, Integer value) {
            setValue(object, value.intValue());
        }

    }


    public static Builder just(View view) {
        return Builder.create(view);
    }

    public static Builder just() {
        return Builder.create();
    }


    /**
     * 动画创建器类，构造者模式
     * 用来辅助创建动画
     *
     * */
    public static class Builder{

        //===================static final=================================\\
        public static final String TYPE_X = "x";
        public static final String TYPE_Y = "y";
        public static final String TYPE_Z = "z";
        public static final String TYPE_TRANS_X = "translationX";
        public static final String TYPE_TRANS_Y = "translationY";
        public static final String TYPE_TRANS_Z = "translationZ";
        public static final String TYPE_SCALE_X = "scaleX";
        public static final String TYPE_SCALE_Y = "scaleY";
        public static final String TYPE_ROTATE = "rotation";
        public static final String TYPE_ROTATE_X = "rotationX";
        public static final String TYPE_ROTATE_Y = "rotationY";
        public static final String TYPE_ALPHA = "alpha";

        //======================variable===============================\\
        // 动画执行的对象
        private View target;

        // 自定义整数类型动画的整型值
        private int[] intValues;
        // 自定义浮点类型动画的浮点值
        private float[] floatValues;
        // 自定义颜色类型动画的整数值
        private @ColorInt int[] colorValues;
        // 视图的饱和度渐变值
        private float[] saturationFValues;

        // 标志是否有传入动画目标视图
        private boolean hasTarget = false;
        // 标志是否有饱和度动画
        private boolean saturation = false;
        // 标志是否有整数类型的动画
        private boolean intValue = false;
        // 标志是否有浮点类型的动画
        private boolean floatValue = false;
        // 标志是否有颜色渐变的动画
        private boolean colorValue = false;

        // 自定义监听器，采用装饰者模式把这些监听器装饰到动画自带的监听器中
        private StartListener start;          // 监听开始
        private EndListener end;              // 监听结束
        private UpdateListener updateInt;     // 监听整数类型的更新
        private UpdateListener updateFloat;   // 监听浮点累心的更新
        private UpdateListener updateColor;   // 监听颜色类型的更新
        private CancelListener cancel;        // 监听取消
        private PauseListener pause;          // 监听暂停
        private ResumeListener resume;        // 监听恢复
        private RepeatListener repeat;        // 监听重复
        // 系统自带监听器
        private Animator.AnimatorListener animatorListener;    // 动画监听器，不监听重复，用于包装以上自定义监听器
        private Animator.AnimatorPauseListener pauseListener;  // 暂停监听器，用于包装以上自定义动画监听器
        private Animator.AnimatorListener repeatListener;      // 动画监听器，只监听重复，用于包装以上自定义动画监听器

        // 动画重复次数
        private int repeatCount = 0;
        // 动画重复模式
        private int repeatMode = Animation.RESTART;
        // 动画执行时间
        private long duration = 300L;
        // 动画插值器
        private Interpolator interpolator;
        // 存放普通动画渐变值得hashMap,例如View的平移、缩放、旋转、透明度动画的渐变值
        private Map<String, float[]> normalAnimatorMap;


        //===================public method===================================\\


        /**
         * Builder创建器
         *
         * @param target 执行动画的目标视图
         * @return 动画构造器
         * @deprecated use {@code AnimUtils.just(View) } directly
         * */
        public static Builder create(@NonNull View target){
            return new Builder(target);
        }

        /**
         * Builder创建器
         * 调用该方法，如果没有调用 {@code setTarget(View)}来设置目标对象，需要为动画添加
         * 更新监听器{@code onUpdate(UpdateListener) } 手动设置动画的对应属性值，
         * 需要注意的是，更新监听器只对{@code setInt（int）}、
         * {@code setFloat(float)}\{@code setColor(int)} 函数所设置的值有限
         * @see AnimUtils.Builder#setTarget(View)
         * @see AnimUtils.Builder#setX(float...)
         * @see AnimUtils.Builder#setY(float...)
         * @see AnimUtils.Builder#setZ(float...)
         * @see AnimUtils.Builder#setTranX(float...)
         * @see AnimUtils.Builder#setTranY(float...)
         * @see AnimUtils.Builder#setTranZ(float...)
         * @see AnimUtils.Builder#setRotate(float...)
         * @see AnimUtils.Builder#setRotateX(float...)
         * @see AnimUtils.Builder#setRotateY(float...)
         * @see AnimUtils.Builder#setScaleX(float...)
         * @see AnimUtils.Builder#setScaleY(float...)
         * @see AnimUtils.Builder#setAlpha(float...)
         * @see AnimUtils.Builder#setSaturation(float...)
         * @see AnimUtils.Builder#setInt(int...)
         * @see AnimUtils.Builder#setFloat(float...)
         * @see AnimUtils.Builder#setColor(int...)
         * @return 动画构造器
         * @deprecated use {@code AnimUtils.just() } directly
         * */
        public static Builder create() {
            return new Builder();
        }

        /**
         * 为动画设置目标对象，只有在这里或者在{@code create(View)} 中设置了，
         * 使用平移，旋转，伸缩，透明度和饱和度渐变动画才能生效，这些动画的设置函数如下
         * @see AnimUtils.Builder#setX(float...)
         * @see AnimUtils.Builder#setY(float...)
         * @see AnimUtils.Builder#setZ(float...)
         * @see AnimUtils.Builder#setTranX(float...)
         * @see AnimUtils.Builder#setTranY(float...)
         * @see AnimUtils.Builder#setTranZ(float...)
         * @see AnimUtils.Builder#setRotate(float...)
         * @see AnimUtils.Builder#setRotateX(float...)
         * @see AnimUtils.Builder#setRotateY(float...)
         * @see AnimUtils.Builder#setScaleX(float...)
         * @see AnimUtils.Builder#setScaleY(float...)
         * @see AnimUtils.Builder#setAlpha(float...)
         * @see AnimUtils.Builder#setSaturation(float...)
         *
         *
         *
         * @param target 动画的目标视图对象
         * @return 动画构造器
         * */
        public Builder setTarget(View target) {
            this.target = target;
            hasTarget = true;
            return this;
        }

        /**
         * 调用该函数设置平移X轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setTranX(float...values) {
            normalAnimatorMap.put(TYPE_TRANS_X, values);
            return this;
        }

        /**
         * 调用该函数设置平移Y轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setTranY(float...values) {
            normalAnimatorMap.put(TYPE_TRANS_Y, values);
            return this;
        }

        /**
         * 调用该函数设置平移Z轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setTranZ(float...values) {
            normalAnimatorMap.put(TYPE_TRANS_Z, values);
            return this;
        }

        /**
         * 调用该函数设置X轴坐标动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setX(float...values){
            normalAnimatorMap.put(TYPE_X, values);
            return this;
        }

        /**
         * 调用该函数设置Y轴坐标动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setY(float... values) {
            normalAnimatorMap.put(TYPE_Y, values);
            return this;
        }

        /**
         * 调用该函数设置Z轴坐标动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setZ(float... values) {
            normalAnimatorMap.put(TYPE_Z, values);
            return this;
        }

        /**
         * 调用该函数设置伸缩X轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setScaleX(float...values) {
            normalAnimatorMap.put(TYPE_SCALE_X, values);
            return this;
        }

        /**
         * 调用该函数设置伸缩X轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setScaleY(float...values) {
            normalAnimatorMap.put(TYPE_SCALE_Y, values);
            return this;
        }

        /**
         * 调用该函数设置旋转Z轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setRotate(float... values) {
            normalAnimatorMap.put(TYPE_ROTATE, values);
            return this;
        }

        /**
         * 调用该函数设置旋转X轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setRotateX(float... values) {
            normalAnimatorMap.put(TYPE_ROTATE_X, values);
            return this;
        }

        /**
         * 调用该函数设置旋转Y轴动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setRotateY(float... values) {
            normalAnimatorMap.put(TYPE_ROTATE_Y, values);
            return this;
        }

        /**
         * 调用该函数设置透明度渐变动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setAlpha(@FloatRange(from = 0f, to = 1f) float... values) {
            normalAnimatorMap.put(TYPE_ALPHA, values);
            return this;
        }

        /**
         * 调用该函数设置饱和度渐变动画，前提是要在动画执行前设置目标对象
         * @see AnimUtils.Builder#create(View)
         * @see AnimUtils.Builder#setTarget(View)
         * 否则设置该值无效
         *
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setSaturation(
                @FloatRange(from = 0f,to = 1f) float...values) {
            saturationFValues = values;
            saturation = true;
            return this;
        }

        /**
         * 调用该函数设置整数类型的动画，该值得设置与是否有设置目标对象无关
         * 如果有设置该值，则需要设置更新监听器{@code onUpdateInt(UpdateListener)}手动更新动画
         * @see AnimUtils.Builder#onUpdateInt(UpdateListener)
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setInt(int...values){
            intValues = values;
            intValue = true;
            return this;
        }

        /**
         * 调用该函数设置整数类型的动画，该值得设置与是否有设置目标对象无关
         * 如果有设置该值，则需要设置更新监听器{@code onUpdateFloat(UpdateListener)}手动更新动画
         * @see AnimUtils.Builder#onUpdateFloat(UpdateListener)
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setFloat(float... values){
            floatValues = values;
            floatValue = true;
            return this;
        }

        /**
         * 调用该函数设置整数类型的动画，该值得设置与是否有设置目标对象无关
         * 如果有设置该值，则需要设置更新监听器{@code onUpdateColor(UpdateListener)}手动更新动画
         * @see AnimUtils.Builder#onUpdateColor(UpdateListener)
         *
         *
         * @param values 动画渐变值
         * @return 动画构造器
         * */
        public Builder setColor(@ColorInt int...values) {
            colorValues = values;
            colorValue = true;
            return this;
        }

        /**
         *
         * 设置开始监听器，在动画开始时调用
         *
         *
         * @param start 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onStart(@NonNull StartListener start) {
            this.start = start;
            return this;
        }

        /**
         *
         * 设置结束监听器，在动画结束时调用
         *
         *
         * @param end 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onEnd(@NonNull EndListener end) {
            this.end = end;
            return this;
        }

        /**
         *
         * 设置暂停监听器
         *
         *
         * @param pause 动画开始监听器，在动画开始是调用
         * @return 动画构造器
         * */
        public Builder onPause(@NonNull PauseListener pause) {
            this.pause = pause;
            return this;
        }

        /**
         *
         * 设置更新整形渐变值监听器，在动画更新时调用
         * 当调用{@code setInt(int...)}时，要调用该方法，两者同时调用才有效
         * @see AnimUtils.Builder#setInt(int...)
         *
         *
         * @param update 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onUpdateInt(@NonNull UpdateListener update) {
            this.updateInt = update;
            return this;
        }

        /**
         *
         * 设置更新浮点渐变值监听器，在动画更新时调用
         * 当调用{@code setFloat(float...)}时，要调用该方法，两者同时调用才有效
         * @see AnimUtils.Builder#setFloat(float...)
         *
         *
         * @param update 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onUpdateFloat(@NonNull UpdateListener update) {
            this.updateFloat = update;
            return this;
        }

        /**
         *
         * 设置更新颜色渐变值监听器，在动画更新时调用
         * 当调用{@code setColor(int...)}时，要调用该方法，两者同时调用才有效
         * @see AnimUtils.Builder#setColor(int...)
         *
         *
         *
         *
         * @param update 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onUpdateColor(@NonNull UpdateListener update) {
            this.updateColor = update;
            return this;
        }

        /**
         *
         * 设置恢复动画监听器，在动画恢复时调用
         *
         *
         * @param resume 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onResume(@NonNull ResumeListener resume) {
            this.resume = resume;
            return this;
        }

        /**
         *
         * 设置重复监听器，在动画重复时调用
         *
         *
         * @param repeat 动画开始监听器
         * @return 动画构造器
         * */
        public Builder onRepeat(@NonNull RepeatListener repeat) {
            this.repeat = repeat;
            return this;
        }

        /**
         *
         * 设置取消监听器，在动画取消时调用
         *
         *
         * @param cancel 动画曲线监听器
         * @return 动画构造器
         * */
        public Builder onCancel(@NonNull CancelListener cancel) {
            this.cancel = cancel;
            return this;
        }

        /**
         *
         * 设置动画重复次数
         *
         *
         * @param count 重复次数
         * @return 动画构造器
         * */
        public Builder setRepeatCount(@IntRange(from = 0) int count) {
            repeatCount = count;
            return this;
        }

        /**
         *  设置动画重复的模式，有两个可选值，分别是
         *  ValueAnimator.RESTART 和 ValueAnimator.REVERSE
         *
         * @param mode 动画重复模式
         * @return 动画构造器
         * */
        public Builder setRepeatMode(int mode){
            repeatMode = mode;
            return this;
        }

        /**
         * 设置动画的持续时间
         *
         * @param duration 动画持续时间
         * @return 动画构造器
         * */
        public Builder setDuration(@IntRange(from = 0) long duration) {
            this.duration = duration;
            return this;
        }

        /**
         * 设置动画插值器
         *
         * @param interpolator 动画插值器
         * @return 动画构造器
         * */
        public Builder setInterpolator(@NonNull Interpolator interpolator){
            this.interpolator = interpolator;
            return this;
        }

        /**
         * 创建并启动动画，该方法在所有参数都设置完后调用
         * 调用该方法后动画将执行
         * @see Builder#build()
         *
         * */
        public void animate() {
            build().start();
        }

        /**
         * 创建动画,该方法在所有参数设置完后调用
         * 调用该方法生成动画，但是需要调用{@code start()} 才执行动画
         * @return 生成的动画集
         * */
        public AnimatorSet build(){

            AnimatorSet set = new AnimatorSet();
            set.setDuration(duration);
            set.setInterpolator(interpolator);
            set.addListener(animatorListener);
            set.addPauseListener(pauseListener);


            AnimatorSet.Builder builder = null;

            // 组合普通的视图动画，例如旋转，平移,透明度或缩放
            builder = composeNormalAnimators(builder,set);

            // 创建色彩饱和度动画
            builder = buildSaturationAnimator(builder, set);

            // 创建自定义整数渐变的动画
            builder = buildIntAnimator(builder, set);

            // 创建自定义浮点数渐变的动画
            builder = buildFloatAnimator(builder, set);

            // 创建自定义的颜色渐变的动画
            builder = buildColorAnimator(builder,set);

            return set;
        }

        //========================private method==================================\\

        // 私有化构造函数
        private Builder(View target) {
            this.target = target;
            hasTarget = true;
            normalAnimatorMap = new HashMap<>();
            initListener();
        }

        // 私有化构造函数
        private Builder() {
            hasTarget = false;
            normalAnimatorMap = new HashMap<>();
            initListener();
        }

        // 初始化所有的监听器
        private void initListener(){
            // 默认空实现
            this.start = animator ->{};
            this.end = animator -> {};
            this.pause = animator -> {};
            this.resume = animator -> {};
            this.updateInt = animator -> {};
            this.updateFloat = this.updateInt;
            this.updateColor = this.updateInt;
            this.repeat = animator -> {};
            this.cancel = animator -> {};

            animatorListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    start.onStart(animator);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    end.onEnd(animator);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    cancel.onCancel(animator);
                }
            };
            pauseListener = new Animator.AnimatorPauseListener() {
                @Override
                public void onAnimationPause(Animator animator) {
                    pause.onPause(animator);
                }

                @Override
                public void onAnimationResume(Animator animator) {
                    resume.onResume(animator);
                }
            };
            repeatListener = new AnimatorListenerAdapter() {
                private boolean repeated = false;
                @Override
                public void onAnimationRepeat(Animator animator) {
                    if (!repeated){
                        repeat.onRepeat(animator);
                        repeated = true;
                    }
                }
            };
        }

        // 组合动画
        private AnimatorSet.Builder compose(
                AnimatorSet set,
                AnimatorSet.Builder builder,
                ValueAnimator animator) {

            return builder==null? set.play(animator):builder.with(animator);
        }

        // 为动画设置重复次数和重复模式
        private void setRepeat(ValueAnimator animator){
            if (repeatCount>0){
                animator.setRepeatCount(repeatCount);
                if (repeatMode==Animation.REVERSE){
                    animator.setRepeatMode(ValueAnimator.REVERSE);
                }else{
                    animator.setRepeatMode(ValueAnimator.RESTART);
                }
                animator.addListener(repeatListener);
            }

        }

        // 组合所有普通动画到set中，即所有渐变值存放在 normalAnimatorMap 中的所有动画
        private AnimatorSet.Builder composeNormalAnimators(AnimatorSet.Builder builder, AnimatorSet set) {
            if (hasTarget && normalAnimatorMap.size()>0) {
                ObjectAnimator animator;
                Iterator<Map.Entry<String, float[]>> iterator
                        = normalAnimatorMap.entrySet().iterator();
                Map.Entry<String,float[]> entry;

                if (builder==null){
                    entry = iterator.next();
                    animator = ObjectAnimator.ofFloat(target,entry.getKey(),entry.getValue());
                    setRepeat(animator);
                    builder = set.play(animator);
                }
                while(iterator.hasNext()) {
                    entry = iterator.next();
                    animator = ObjectAnimator.ofFloat(target,entry.getKey(),entry.getValue());
                    setRepeat(animator);
                    builder =builder.with(animator);

                }
            }
            return builder;
        }

        // 创建饱和度动画，并组合到动画集set中
        private AnimatorSet.Builder buildSaturationAnimator(AnimatorSet.Builder builder, AnimatorSet set) {
            if (saturation && hasTarget) {
                try {
                    ImageView imageView = ((ImageView) target);

                    final ObservableColorMatrix cm = new ObservableColorMatrix();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, saturationFValues);
                    animator.addUpdateListener(v -> imageView.setColorFilter(new
                            ColorMatrixColorFilter(cm)));

                    animator.addListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationStart(Animator animation) {
                            imageView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            imageView.clearColorFilter();
                            imageView.setHasTransientState(false);
                        }
                    });

                    setRepeat(animator);

                    builder = compose(set,builder,animator);

                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
            return builder;
        }

        // 创建整形渐变的自定义动画，并组合到动画集中
        private AnimatorSet.Builder buildIntAnimator(AnimatorSet.Builder builder, AnimatorSet set){

            if (intValue) {
                ValueAnimator animator = ValueAnimator.ofInt(intValues);
                animator.addUpdateListener(this.updateInt::onUpdate);
                setRepeat(animator);
                builder = compose(set,builder,animator);
            }
            return builder;
        }

        // 创建浮点渐变的自定义动画，并组合到动画集中
        private AnimatorSet.Builder buildFloatAnimator(AnimatorSet.Builder builder, AnimatorSet set){
            if (floatValue) {
                ValueAnimator animator = ValueAnimator.ofFloat(floatValues);
                animator.addUpdateListener(this.updateFloat::onUpdate);
                setRepeat(animator);
                builder = compose(set,builder,animator);
            }
            return builder;
        }

        // 创建颜色渐变的自定义动画，并组合到动画集中
        private AnimatorSet.Builder buildColorAnimator(AnimatorSet.Builder builder, AnimatorSet set){

            if (colorValue) {
                ValueAnimator animator = ValueAnimator.ofArgb(colorValues);
                animator.addUpdateListener(this.updateColor::onUpdate);
                setRepeat(animator);
                builder = compose(set,builder,animator);
            }
            return builder;
        }

    }

    //========================================================\\
    //=============listeners==================================//

    /**
     * 动画更新监听器
     * */
    public interface UpdateListener{
        void onUpdate(ValueAnimator animator);
    }

    /**
     * 动画开始监听器
     * */
    public interface StartListener{
        void onStart(Animator animator);
    }

    /**
     * 动画结束监听器
     * */
    public interface EndListener{
        void onEnd(Animator animator);
    }

    /**
     * 动画取消监听器
     * */
    public interface CancelListener{
        void onCancel(Animator animator);
    }

    /**
     * 动画重复监听器
     * */
    public interface RepeatListener{
        void onRepeat(Animator animator);
    }

    /**
     * 动画暂停监听器
     * */
    public interface PauseListener{
        void onPause(Animator animator);
    }

    /**
     * 动画回复监听器
     * */
    public interface ResumeListener{
        void onResume(Animator animator);
    }



}
