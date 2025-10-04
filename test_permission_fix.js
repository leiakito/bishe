// 测试权限验证修复的脚本
const testPermissionFix = () => {
  console.log('=== 测试权限验证修复 ===');
  
  // 模拟竞赛数据（从后端API获取的实际数据结构）
  const competition = {
    id: 2,
    name: "挖掘机",
    creator: {
      id: 54,
      username: "199810",
      realName: "2323232323",
      role: "TEACHER"
    }
  };
  
  // 模拟当前用户数据
  const currentUser1 = {
    id: 54,  // 数字类型，与creator.id相同
    username: "199810"
  };
  
  const currentUser2 = {
    id: "54",  // 字符串类型，与creator.id相同但类型不同
    username: "199810"
  };
  
  const currentUser3 = {
    id: 55,  // 不同的用户ID
    username: "teacher2"
  };
  
  // 原始的权限验证逻辑（有问题的）
  const oldPermissionCheck = (comp, user) => {
    return comp.creator?.id === user?.id;
  };
  
  // 修复后的权限验证逻辑
  const newPermissionCheck = (comp, user) => {
    const creatorId = String(comp.creator?.id || '');
    const currentUserId = String(user?.id || '');
    return creatorId === currentUserId && creatorId !== '';
  };
  
  console.log('测试场景1: 数字ID匹配');
  console.log('旧逻辑结果:', oldPermissionCheck(competition, currentUser1));
  console.log('新逻辑结果:', newPermissionCheck(competition, currentUser1));
  
  console.log('\n测试场景2: 字符串vs数字ID匹配');
  console.log('旧逻辑结果:', oldPermissionCheck(competition, currentUser2));
  console.log('新逻辑结果:', newPermissionCheck(competition, currentUser2));
  
  console.log('\n测试场景3: ID不匹配');
  console.log('旧逻辑结果:', oldPermissionCheck(competition, currentUser3));
  console.log('新逻辑结果:', newPermissionCheck(competition, currentUser3));
  
  console.log('\n=== 测试完成 ===');
};

// 运行测试
testPermissionFix();