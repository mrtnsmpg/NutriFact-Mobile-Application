package com.example.nutrifact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {
    Context context;

    public SliderAdapter(Context context){
        this.context = context;
    }

    public int[] slide_images = {
            R.drawable.ic_undraw_millennial_girl_fxlt,
            R.drawable.undraw_insert_block_re_4t4l,
            R.drawable.ic_undraw_add_information_j2wg
    };

    private String[] slider_texts = {
            "WELCOME",
            "CAPTURING FRUIT IMAGE",
            "FRUIT NUTRITIONAL INFORMATION"
    };

    private String[] slider_desc = {
            "Nutrifact is a fruit nutrition chechek mobile application " +
                    "that used to assist fruit consumers in identifying fruit " +
                    "variety and know its nutritional information. This application " +
                    "uses image recognition for capturing fruit indentity. Let's get " +
                    "healthy together so we can live forever!",

            "To get started, you have to place your " +
                    "fruit to your palm and bring out the " +
                    "NutriFact application. Once you are " +
                    "on a camera page, just click the shutter " +
                    "button and learn more about the fruits. ",

            "After capturing your fruit, " +
                    "the application will give you fruit nutritional information. " +
                    "You  will also know some of the fruit identification name and textures. "
    };

    @Override
    public int getCount() {
        return slider_texts.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.welcome_sub, container, false);

        ImageView imageView = view.findViewById(R.id.welcomeImage);
        TextView mTitleText = view.findViewById(R.id.welcomeTitle);
        TextView mDescriptionText = view.findViewById(R.id.welcomeDescription);

        imageView.setImageResource(slide_images[position]);
        mTitleText.setText(slider_texts[position]);
        mDescriptionText.setText(slider_desc[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout)object);
    }
}
