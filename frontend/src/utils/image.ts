import placeholderFood from '@/assets/placeholder-food.png'

/** 供 <img :src> 直接绑定的本地占位图（OSS 桶失效时兜底） */
export const foodPlaceholder = placeholderFood

/**
 * 图片加载失败兜底：把 src 替换为本地占位图。
 * 用法：<img :src="dish.image" @error="handleImageError" />
 * 加保护避免占位图本身加载失败时无限递归。
 */
export function handleImageError(event: Event): void {
  const img = event.target as HTMLImageElement
  if (img && !img.src.endsWith(placeholderFood)) {
    img.src = placeholderFood
  }
}
