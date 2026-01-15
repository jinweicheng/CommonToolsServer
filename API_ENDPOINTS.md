# API端点说明

## 统计接口

### 上报使用统计
- **URL**: `http://localhost:8080/api/statistics/usage`
- **方法**: `POST`
- **Content-Type**: `application/json`

**请求示例**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "statistics": [
    {
      "module": "watermark",
      "action": "upload",
      "endpoint": "/tools/watermark",
      "ipAddress": null,
      "userAgent": "Mozilla/5.0...",
      "deviceType": "PC",
      "browser": "Chrome",
      "os": "Windows 10/11",
      "statDate": "2026-01-13",
      "statHour": 14
    }
  ]
}
```

**响应示例**:
```json
{
  "success": true,
  "message": "Statistics recorded successfully",
  "recorded": 1
}
```

## 注意事项

1. **后端端口**: 8080
2. **Context Path**: `/api`
3. **完整路径**: `http://localhost:8080/api/statistics/usage`

## 前端代理配置建议

如果前端运行在3000端口，需要在`vite.config.js`或`webpack.config.js`中配置代理：

### Vite配置示例
```javascript
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

### Webpack配置示例
```javascript
module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```

这样前端就可以使用 `http://localhost:3000/api/statistics/usage` 访问后端接口了。
