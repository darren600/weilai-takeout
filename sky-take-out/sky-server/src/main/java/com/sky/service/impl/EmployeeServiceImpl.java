package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 后期需要进行对前端传过来的密码进行md5加密，然后再进行比对    怕程序员遗忘的收尾工作标记标签
         password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 最终是新增员工全部信息到数据库
        Employee employee = new Employee();
        //对象属性拷贝  前提是属性名要一致（source desc）
        BeanUtils.copyProperties(employeeDTO, employee);
        //其他属性也要添加
        //状态可以复用
        employee.setStatus(StatusConstant.ENABLE);
        // 新增员工时设置默认密码（605163），MD5 加密后入库
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //创建时间和修改时间设置为当前时间
        //创建人和修改人设置为当前登录的员工id

        //employee.setCreateTime(LocalDateTime.now());
      //  employee.setUpdateTime(LocalDateTime.now());
        //通过threadlocal获取当前登录的员工.这个项目定义的时候BaseContext这个类当中
     //   employee.setCreateUser(BaseContext.getCurrentId());
       // employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // MyBatis-Plus 分页：构造 Page 作为第一个入参，全局分页拦截器自动拼 limit 并统计 total
        Page<Employee> page = new Page<>(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        employeeMapper.pageQuery(page, employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getRecords();
        PageResult pageResult = new PageResult(total, records);
        return pageResult;
    }

    @Override
    public void startOrStop(Integer status,Long id) {
//要动态存储
     /*   Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        Employee employee =  Employee.builder().
                status(status)
                .id(id)
                .build();

        employeeMapper.update(employee);
    }


    /*
    * 根据ID查询员工信息
    * @param id
    * @return
    * */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
//        处理密码
        employee.setPassword("****");
        return employee;
    }


    /*
    * 修改员工信息
    * @param employeeDTO
    * @return
    * */
    @Override
    public void updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
//        对象的属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);

    }

}
