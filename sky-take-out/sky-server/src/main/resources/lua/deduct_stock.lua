-- 库存原子扣减脚本
-- KEYS[1]: 库存 key
-- ARGV[1]: 本次扣减数量
-- 返回: -2 未初始化, -1 库存不足, >=0 扣减后的剩余库存
local key = KEYS[1]
local num = tonumber(ARGV[1])
local stock = tonumber(redis.call('GET', key) or '-1')
if stock < 0 then
  return -2
end
if stock < num then
  return -1
end
redis.call('DECRBY', key, num)
return stock - num
