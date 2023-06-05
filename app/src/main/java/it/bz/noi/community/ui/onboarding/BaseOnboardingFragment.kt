// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentOnboardingBinding

open class BaseOnboardingFragment : Fragment() {

	private var _binding: FragmentOnboardingBinding? = null
	internal val binding get() = _binding!!

	private val gradientDrawable = PaintDrawable().apply {
		val sf: ShapeDrawable.ShaderFactory = object : ShapeDrawable.ShaderFactory() {
			override fun resize(width: Int, height: Int): Shader {
				return LinearGradient(
					0f, height.toFloat(), 0f, 0f,
					listOf(
						ColorUtils.setAlphaComponent(
							requireContext().getColor(R.color.background_color),
							255
						), // alpha = 100%
						ColorUtils.setAlphaComponent(
							requireContext().getColor(R.color.background_color),
							222
						), // alpha = 87%
						ColorUtils.setAlphaComponent(
							requireContext().getColor(R.color.background_color),
							153
						), // alpha = 60%
						ColorUtils.setAlphaComponent(
							requireContext().getColor(R.color.background_color),
							0
						), // alpha = 0%
					).toIntArray(),
					listOf(0f, 0.38f, 0.72f, 1f).toFloatArray(),
					Shader.TileMode.CLAMP
				)
			}
		}

		shape = RectShape()
		shaderFactory = sf
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentOnboardingBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.image.foreground = gradientDrawable
	}

}
