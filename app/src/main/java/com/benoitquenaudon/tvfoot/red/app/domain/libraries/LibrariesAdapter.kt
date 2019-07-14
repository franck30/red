package com.benoitquenaudon.tvfoot.red.app.domain.libraries

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.benoitquenaudon.tvfoot.red.R
import com.benoitquenaudon.tvfoot.red.databinding.LibraryRowBinding
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class LibrariesAdapter @Inject constructor(
    val libraries: List<Library>
) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {
  val libraryClickObservable: PublishSubject<Library> = PublishSubject.create()

  override fun getItemCount(): Int = libraries.size + 1

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    if (holder is LibraryViewHolder) {
      holder.bind(libraries[position - 1]) // adjust for header
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    if (viewType == VIEW_TYPE_HEADER) {
      return LibraryHeaderViewHolder(
          LayoutInflater.from(parent.context).inflate(R.layout.library_header_row, parent, false))
    } else if (viewType == VIEW_TYPE_LIBRARY) {
      val layoutInflater = LayoutInflater.from(parent.context)
      val binding: LibraryRowBinding =
          DataBindingUtil.inflate(
              layoutInflater,
              R.layout.library_row,
              parent, false)
      return LibraryViewHolder(binding, this)
    }
    throw IllegalStateException("unknown viewType $viewType")
  }

  override fun getItemViewType(position: Int): Int {
    return if (position == 0) VIEW_TYPE_HEADER else VIEW_TYPE_LIBRARY
  }

  fun onClick(library: Library) {
    libraryClickObservable.onNext(library)
  }

  companion object Constant {
    const val VIEW_TYPE_HEADER: Int = 0
    const val VIEW_TYPE_LIBRARY: Int = 1
  }
}