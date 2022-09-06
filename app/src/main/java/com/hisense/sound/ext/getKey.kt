package com.hisense.sound.ext

/**
 * 根据map集合的值获取键的扩展函数
 *
 * @param map mao集合
 * @param target 值
 * @param K 键
 * @param V 值
 * @return 键
 */
internal fun <K, V> getKey(map: Map<K, V>, target: V): K {
    return map.keys.first { target == map[it] }
}