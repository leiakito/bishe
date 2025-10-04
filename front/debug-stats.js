// 测试统计 API 的脚本
async function testStatsAPI() {
    try {
        console.log('开始测试统计 API...');
        
        // 直接调用后端 API
        const response = await fetch('http://localhost:8080/api/competitions/stats', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        console.log('API 响应状态:', response.status);
        
        const data = await response.json();
        console.log('API 响应数据:', JSON.stringify(data, null, 2));
        
        // 检查数据结构
        if (data.success && data.data) {
            const stats = data.data;
            console.log('统计数据解析:');
            console.log('- 总竞赛数:', stats.totalCompetitions);
            console.log('- 待审核:', stats.pendingApproval);
            console.log('- 进行中:', stats.inProgress);
            console.log('- 已完成:', stats.completed);
            console.log('- 已发布:', stats.published);
            console.log('- 草稿:', stats.draft);
            console.log('- 已取消:', stats.cancelled);
        } else {
            console.error('API 响应格式不正确:', data);
        }
        
    } catch (error) {
        console.error('API 调用失败:', error);
    }
}

// 执行测试
testStatsAPI();