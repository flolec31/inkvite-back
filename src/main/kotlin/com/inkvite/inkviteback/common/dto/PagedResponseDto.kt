package com.inkvite.inkviteback.common.dto

data class PagedResponseDto<T>(
    val content: List<T>,
    val total: Long,
    val page: Int,
    val pageCount: Int
)