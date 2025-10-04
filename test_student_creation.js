// 测试学生创建API的脚本
// 这个脚本可以在浏览器控制台中运行来测试学生创建功能

async function testStudentCreation() {
  try {
    // 首先登录管理员账户
    const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username: 'admin',
        password: 'admin123'
      })
    });
    
    const loginData = await loginResponse.json();
    console.log('登录响应:', loginData);
    
    if (!loginData.success) {
      console.error('登录失败:', loginData.message);
      return;
    }
    
    const token = loginData.data.token;
    
    // 创建测试学生
    const studentData = {
      username: 'test_student_' + Date.now(),
      password: 'password123',
      realName: '测试学生',
      email: 'test@example.com',
      phone: '13800138000',
      studentId: 'STU' + Date.now(), // 使用 studentId 而不是 studentNumber
      college: '计算机学院',
      major: '软件工程',
      grade: '2024',
      className: '软工1班',
      gender: 'MALE',
      role: 'STUDENT'
    };
    
    console.log('准备创建学生，数据:', studentData);
    
    const createResponse = await fetch('http://localhost:8080/api/users/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(studentData)
    });
    
    const createData = await createResponse.json();
    console.log('创建学生响应:', createData);
    
    if (createData.success) {
      console.log('✅ 学生创建成功！');
      
      // 验证数据库中的数据
      const userId = createData.data.id;
      console.log('新创建的学生ID:', userId);
      
      // 获取用户详情来验证 student_id 是否正确保存
      const userResponse = await fetch(`http://localhost:8080/api/users/${userId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const userData = await userResponse.json();
      console.log('用户详情:', userData);
      
      if (userData.data && userData.data.studentId) {
        console.log('✅ student_id 字段已正确保存:', userData.data.studentId);
      } else {
        console.log('❌ student_id 字段未保存或为空');
      }
    } else {
      console.error('❌ 学生创建失败:', createData.message);
    }
    
  } catch (error) {
    console.error('测试过程中发生错误:', error);
  }
}

// 运行测试
console.log('开始测试学生创建功能...');
testStudentCreation();