// 根据角色获取教师列表
export const getTeachersByRole = async (params?: TeacherQueryParams) => {
  try {
    const response = await request.get<any>("/api/admin/users/role/TEACHER", {
      page: (params?.page || 1) - 1, // 后端使用0-based分页
      size: params?.size || 10
    })
    
    console.log("getTeachersByRole - 原始响应:", response)
    console.log("getTeachersByRole - response.data:", response.data)
    
    // 处理后端响应格式
    const responseData = response.data || response
    console.log("getTeachersByRole - responseData:", responseData)
    
    const teachers = responseData.data || responseData || []
    console.log("getTeachersByRole - teachers:", teachers)
    
    return wrapResponse({
      success: true,
      data: teachers,
      totalElements: responseData.totalElements || teachers.length,
      totalPages: responseData.totalPages || 1,
      currentPage: (responseData.currentPage || 0) + 1 // 转换为1-based分页
    })
  } catch (error) {
    console.error("根据角色获取教师列表失败:", error)
    return {
      success: false,
      message: "获取教师列表失败",
      data: []
    }
  }
}
