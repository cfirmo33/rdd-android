package lv.rigadevday.android.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.ViewTarget
import jp.wasabeef.glide.transformations.CropCircleTransformation
import lv.rigadevday.android.R

private fun ImageView.getFetcherInstance(url: String) = Glide
    .with(this.context)
    .load(url.toImageUrl())
    .diskCacheStrategy(DiskCacheStrategy.SOURCE)


fun ImageView.loadCircleAvatar(url: String): Target<GlideDrawable> = getFetcherInstance(url)
    .centerCrop()
    .bitmapTransform(CropCircleTransformation(this.context))
    .into(this)

fun ImageView.loadSquareAvatar(url: String): Target<GlideDrawable> = getFetcherInstance(url)
    .placeholder(R.drawable.vector_speaker_placeholder)
    .centerCrop()
    .into(this)

fun ImageView.loadLogo(url: String, onDone: () -> Unit): Target<GlideDrawable> = getFetcherInstance(url)
    .fitCenter()
    .into(object : ViewTarget<ImageView, GlideDrawable>(this) {
        override fun onResourceReady(resource: GlideDrawable, a: GlideAnimation<in GlideDrawable>) {
            view.setImageDrawable(resource)
            onDone()
        }

    })

fun ImageView.loadVenueImage(url: String): Target<GlideDrawable> = getFetcherInstance(url)
    .centerCrop()
    .into(this)
