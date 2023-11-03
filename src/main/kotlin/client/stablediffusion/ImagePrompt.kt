package client.stablediffusion

import kotlinx.serialization.Serializable
import settings

/*@Serializable
data class ImagePrompt(
    val prompt: String = "",
    val seed: Int = -1,
    val steps: Int = 20,
    val cfg_scale: Int = 7,
    val width: Int = 192,
    val height: Int = 128,
    val negative_prompt: String = "",
    val sampler_index: String = "Euler",
    val save_images: Boolean = false,
) {
    companion object {
        fun createPrompt(userText: String, text: String): ImagePrompt {
            return ImagePrompt(
                prompt = "$userText. $text.",
                seed = settings.imageGenerating.seed,
                steps = settings.imageGenerating.steps,
                cfg_scale = settings.imageGenerating.cfg_scale,
                width = settings.imageGenerating.width,
                height = settings.imageGenerating.height,
                negative_prompt = settings.imageGenerating.negative_prompt,
                sampler_index = settings.imageGenerating.sampler_index,
                save_images = settings.imageGenerating.save_images,
            )
        }
    }
}*/

@Serializable
data class ImagePrompt(
    val enable_hr: Boolean = false,
    val denoising_strength: Int = 0,
    val firstphase_width: Int = 0,
    val firstphase_height: Int = 0,
    val hr_scale: Int = 2,
    val hr_upscaler: String = "",
    val hr_second_pass_steps: Int = 0,
    val hr_resize_x: Int = 0,
    val hr_resize_y: Int = 0,
    val prompt: String = "",
    val styles: List<String> = listOf(),
    val seed: Int = -1,
    val subseed: Int = -1,
    val subseed_strength: Int = 0,
    val seed_resize_from_h: Int = -1,
    val seed_resize_from_w: Int = -1,
    val sampler_name: String = "",
    val batch_size: Int = 1,
    val n_iter: Int = 1,
    val steps: Int = 50,
    val cfg_scale: Int = 7,
    val width: Int = 512,
    val height: Int = 512,
    val restore_faces: Boolean = false,
    val tiling: Boolean = false,
    val do_not_save_samples: Boolean = false,
    val do_not_save_grid: Boolean = false,
    val negative_prompt: String = "",
    val eta: Int = 0,
    val s_churn: Int = 0,
    val s_tmax: Int = 0,
    val s_tmin: Int = 0,
    val s_noise: Int = 1,
    val override_settings_restore_afterwards: Boolean = true,
    val sampler_index: String = "Euler",
    val script_name: String = "",
    val send_images: Boolean = true,
    val save_images: Boolean = false,
) {
    companion object {
        fun createPrompt(prompt: String = ""): ImagePrompt {
            return ImagePrompt(
                prompt = "${settings.image_generating.style}. $prompt.",
                seed = settings.image_generating.seed,
                steps = settings.image_generating.steps,
                cfg_scale = settings.image_generating.cfg_scale,
                width = settings.image_generating.width,
                height = settings.image_generating.height,
                negative_prompt = settings.image_generating.negative_prompt,
                sampler_index = settings.image_generating.sampler_index,
                save_images = settings.image_generating.save_images,
            )
        }
    }
}