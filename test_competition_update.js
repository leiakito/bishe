/**
 * 竞赛更新功能测试脚本
 * 用于验证所有字段是否能正确更新到数据库
 */

const axios = require('axios');

// 配置
const BASE_URL = 'http://localhost:8080';
const TEACHER_USERNAME = '199810';
const TEACHER_PASSWORD = 'a123456';
const COMPETITION_ID = 2; // 挖掘机竞赛

// 测试数据
const TEST_UPDATE_DATA = {
  name: '挖掘机技能竞赛（已更新）',
  description: '这是一个更新后的挖掘机技能竞赛描述',
  location: '北京市朝阳区测试地点',
  organizer: '测试主办方机构',
  contactInfo: '联系电话：010-12345678，邮箱：test@example.com',
  prizeInfo: '一等奖：10000元现金 + 证书\n二等奖：5000元现金 + 证书\n三等奖：2000元现金 + 证书',
  minTeamSize: 2,
  maxTeamSize: 4,
  registrationFee: 100.50,
  rules: '更新后的竞赛规则：\n1. 参赛者必须具备相关技能\n2. 比赛过程中不得作弊\n3. 遵守安全规定'
};

let authToken = '';

/**
 * 教师登录
 */
async function teacherLogin() {
  try {
    console.log('🔐 正在登录教师账户...');
    const response = await axios.post(`${BASE_URL}/api/teacher/login`, {
      username: TEACHER_USERNAME,
      password: TEACHER_PASSWORD
    });

    console.log('📋 登录响应:', JSON.stringify(response.data, null, 2));
    
    // 尝试不同的响应格式
    if (response.data && response.data.token) {
      authToken = response.data.token;
    } else if (response.data && response.data.data && response.data.data.token) {
      authToken = response.data.data.token;
    } else if (response.data && response.data.success && response.data.data && response.data.data.token) {
      authToken = response.data.data.token;
    } else {
      console.error('❌ 无法从响应中获取token:', response.data);
      return false;
    }
    
    console.log('✅ 教师登录成功');
    console.log('📝 Token:', authToken.substring(0, 20) + '...');
    return true;
  } catch (error) {
    console.error('❌ 登录请求失败:', error.response?.data || error.message);
    return false;
  }
}

/**
 * 获取竞赛详情（更新前）
 */
async function getCompetitionBefore() {
  try {
    console.log('\n📋 获取竞赛更新前的数据...');
    const response = await axios.get(`${BASE_URL}/api/competitions/${COMPETITION_ID}`);
    
    if (response.data.success) {
      const competition = response.data.data;
      console.log('📊 更新前的竞赛数据:');
      console.log('  名称:', competition.name);
      console.log('  描述:', competition.description || '无');
      console.log('  地点:', competition.location || '无');
      console.log('  主办方:', competition.organizer || '无');
      console.log('  联系方式:', competition.contactInfo || '无');
      console.log('  奖项设置:', competition.prizeInfo || '无');
      console.log('  最小团队人数:', competition.minTeamSize || '无');
      console.log('  最大团队人数:', competition.maxTeamSize || '无');
      console.log('  报名费用:', competition.registrationFee || '无');
      console.log('  竞赛规则:', competition.rules || '无');
      return competition;
    } else {
      console.error('❌ 获取竞赛详情失败:', response.data.message);
      return null;
    }
  } catch (error) {
    console.error('❌ 获取竞赛详情请求失败:', error.response?.data || error.message);
    return null;
  }
}

/**
 * 更新竞赛
 */
async function updateCompetition() {
  try {
    console.log('\n🔄 正在更新竞赛...');
    console.log('📝 更新数据:', JSON.stringify(TEST_UPDATE_DATA, null, 2));
    
    const response = await axios.put(
      `${BASE_URL}/api/teacher/competitions/${COMPETITION_ID}`,
      TEST_UPDATE_DATA,
      {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      }
    );

    console.log('📋 服务器完整响应:', JSON.stringify(response.data, null, 2));
    
    // 将响应保存到文件以便详细查看
    const fs = require('fs');
    fs.writeFileSync('server_response.json', JSON.stringify(response.data, null, 2));
    
    if (response.data.success) {
      console.log('✅ 竞赛更新成功');
      console.log('📊 服务器响应:', response.data.message);
      return true;
    } else {
      console.error('❌ 竞赛更新失败:', response.data.message);
      return false;
    }
  } catch (error) {
    console.error('❌ 更新竞赛请求失败:', error.response?.data || error.message);
    if (error.response?.status === 401) {
      console.error('🔒 认证失败，请检查token是否有效');
    }
    return false;
  }
}

/**
 * 获取竞赛详情（更新后）
 */
async function getCompetitionAfter() {
  try {
    console.log('\n📋 获取竞赛更新后的数据...');
    // 等待一秒确保数据库更新完成
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    const response = await axios.get(`${BASE_URL}/api/competitions/${COMPETITION_ID}`);
    
    if (response.data.success) {
      const competition = response.data.data;
      console.log('📊 更新后的竞赛数据:');
      console.log('  名称:', competition.name);
      console.log('  描述:', competition.description || '无');
      console.log('  地点:', competition.location || '无');
      console.log('  主办方:', competition.organizer || '无');
      console.log('  联系方式:', competition.contactInfo || '无');
      console.log('  奖项设置:', competition.prizeInfo || '无');
      console.log('  最小团队人数:', competition.minTeamSize || '无');
      console.log('  最大团队人数:', competition.maxTeamSize || '无');
      console.log('  报名费用:', competition.registrationFee || '无');
      console.log('  竞赛规则:', competition.rules || '无');
      return competition;
    } else {
      console.error('❌ 获取竞赛详情失败:', response.data.message);
      return null;
    }
  } catch (error) {
    console.error('❌ 获取竞赛详情请求失败:', error.response?.data || error.message);
    return null;
  }
}

/**
 * 验证更新结果
 */
function verifyUpdate(beforeData, afterData) {
  console.log('\n🔍 验证更新结果...');
  
  const fieldsToCheck = [
    'name', 'description', 'location', 'organizer', 'contactInfo', 
    'prizeInfo', 'minTeamSize', 'maxTeamSize', 'registrationFee', 'rules'
  ];
  
  let allFieldsUpdated = true;
  const results = {};
  
  fieldsToCheck.forEach(field => {
    const expectedValue = TEST_UPDATE_DATA[field];
    const actualValue = afterData[field];
    const isUpdated = actualValue === expectedValue;
    
    results[field] = {
      expected: expectedValue,
      actual: actualValue,
      updated: isUpdated
    };
    
    if (!isUpdated) {
      allFieldsUpdated = false;
    }
    
    const status = isUpdated ? '✅' : '❌';
    console.log(`  ${status} ${field}:`);
    console.log(`    期望值: ${expectedValue}`);
    console.log(`    实际值: ${actualValue}`);
    console.log(`    是否更新: ${isUpdated ? '是' : '否'}`);
  });
  
  console.log('\n📊 验证总结:');
  console.log(`  总字段数: ${fieldsToCheck.length}`);
  console.log(`  成功更新: ${Object.values(results).filter(r => r.updated).length}`);
  console.log(`  更新失败: ${Object.values(results).filter(r => !r.updated).length}`);
  console.log(`  整体结果: ${allFieldsUpdated ? '✅ 所有字段更新成功' : '❌ 部分字段更新失败'}`);
  
  return { allFieldsUpdated, results };
}

/**
 * 主测试函数
 */
async function runTest() {
  console.log('🚀 开始竞赛更新功能测试');
  console.log('=' .repeat(50));
  
  try {
    // 1. 教师登录
    const loginSuccess = await teacherLogin();
    if (!loginSuccess) {
      console.log('❌ 测试终止：登录失败');
      return;
    }
    
    // 2. 获取更新前的数据
    const beforeData = await getCompetitionBefore();
    if (!beforeData) {
      console.log('❌ 测试终止：无法获取竞赛数据');
      return;
    }
    
    // 3. 更新竞赛
    const updateSuccess = await updateCompetition();
    if (!updateSuccess) {
      console.log('❌ 测试终止：更新失败');
      return;
    }
    
    // 4. 获取更新后的数据
    const afterData = await getCompetitionAfter();
    if (!afterData) {
      console.log('❌ 测试终止：无法获取更新后的数据');
      return;
    }
    
    // 5. 验证更新结果
    const { allFieldsUpdated, results } = verifyUpdate(beforeData, afterData);
    
    console.log('\n' + '=' .repeat(50));
    if (allFieldsUpdated) {
      console.log('🎉 测试完成：所有字段更新功能正常！');
    } else {
      console.log('⚠️  测试完成：发现字段更新问题，请检查代码！');
      
      // 输出失败的字段
      const failedFields = Object.entries(results)
        .filter(([_, result]) => !result.updated)
        .map(([field, _]) => field);
      
      console.log('❌ 更新失败的字段:', failedFields.join(', '));
    }
    
  } catch (error) {
    console.error('💥 测试过程中发生错误:', error.message);
  }
}

// 运行测试
if (require.main === module) {
  runTest();
}

module.exports = {
  runTest,
  teacherLogin,
  updateCompetition,
  verifyUpdate
};