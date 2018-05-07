#include "UniversalMediaPlayer.h"

@implementation UniversalMediaPlayer

- (id)init
{
    self = [super init];
    _playerStates = [NSMutableArray array];
    _cachedVolume = -1;
    _cachedRate = -1;
    _tmpBuffering = -1;
    _instance = self;
    
    _renderingAPI = (int)UnitySelectedRenderingAPI();
    _renderingDevice = UnityGetMetalDevice();
    if (_renderingAPI != apiMetal)
        _renderingDevice = UnityGetMainScreenContextGLES();
    
    return _instance;
}

- (void)setupPlayer:(NSString *)options
{
    _player = [[MediaPlayerNative alloc] init];
    [_player setupPlayer:[options stringByReplacingOccurrencesOfString:@":" withString:@""]];
    _player.delegate = self;
    [self setupAudioSession];
}

- (void)setupAudioSession
{
    NSError *categoryError = nil;
    BOOL success = [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback withOptions:AVAudioSessionCategoryOptionMixWithOthers error:&categoryError];
    if (!success)
    {
        NSLog(@"Error setting audio session category: %@", categoryError);
    }
    
    NSError *activeError = nil;
    success = [[AVAudioSession sharedInstance] setActive:YES error:&activeError];
    if (!success)
    {
        NSLog(@"Error setting audio session active: %@", activeError);
    }
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(isAudioRouteChanged:) name:AVAudioSessionRouteChangeNotification object:nil];
}

- (int)framesCounter
{
    return [_player framesCounter];
}

- (CVPixelBufferRef)videoBuffer
{
    return [_player videoBuffer];
}

- (void)setDataSource:(NSString *)path
{
    [_player setDataSource:path];
}

- (void)play
{
    [_player play];
}

- (void)pause
{
    [_player pause];
}

- (void)stop
{
    if (_player != nil)
    {
        [_player stop];
        
        _cachedVolume = [_player getVolume];
        _cachedRate = [_player getPlaybackRate];
        _tmpBuffering = -1;
        _tmpTime = -1;
        
        if(_texture)
        {
            if (_renderingAPI == apiMetal)
                CFRelease(_texture);
            else
            {
                unsigned tex = (unsigned)[self getGLTextureFromCVTextureCache:_texture];
                glDeleteTextures(1, &tex);
            }
            _texture = 0;
        }
        
        if(_textureCache)
        {
            CFRelease(_textureCache);
            _textureCache = 0;
        }
    }
}

- (void)free
{
    [_player free];
}

- (int)getDuration
{
    return [_player duration];
}

- (int)getVolume
{
    if ([self framesCounter] <= 0)
        return _cachedVolume;
    else
        return [_player getVolume];
}

- (void)setVolume:(int)value
{
    if ([self framesCounter] <= 0)
        _cachedVolume = value;
    else
        [_player setVolume:value];
}

- (int)getTime
{
    return [_player getTime];
}

- (void)setTime:(int)value
{
    [_player setTime:value];
}

- (float)getPosition
{
    return [_player getPosition];
}

- (void)setPosition:(float)value
{
    [_player setPosition:value];
}

- (float)getPlaybackRate
{
    if ([self framesCounter] <= 0)
        return _cachedRate;
    return [_player getPlaybackRate];
}

- (void)setPlaybackRate:(float)value
{
    if ([self framesCounter] <= 0)
        _cachedRate = value;
    else
        [_player setPlaybackRate:value];
}

- (bool)isPlaying
{
    return [_player isPlaying];
}

- (bool)isReady
{
    return [_player isReady];
}

- (int)getVideoWidth
{
    return [_player getVideoWidth];
}

- (int)getVideoHeight
{
    return [_player getVideoHeight];
}

- (void)mediaPlayerStateChanged:(PlayerState*)state
{
    [_playerStates queuePush:state];
}

- (uintptr_t)getGLTextureFromCVTextureCache:(void*)texture
{
    if (_renderingAPI == apiMetal)
        return (uintptr_t)(__bridge void*)CVMetalTextureGetTexture((CVMetalTextureRef)texture);
    else
        return CVOpenGLESTextureGetName((CVOpenGLESTextureRef)texture);
}

- (void*)createCVTextureCache
{
    void* ret = 0;
    
    CVReturn err = 0;
    if(_renderingAPI == apiMetal)
        err = CVMetalTextureCacheCreate(kCFAllocatorDefault, 0, _renderingDevice, 0, (CVMetalTextureCacheRef*)&ret);
    else
        err = CVOpenGLESTextureCacheCreate(kCFAllocatorDefault, 0, _renderingDevice, 0, (CVOpenGLESTextureCacheRef*)&ret);
    
    if (err)
    {
        NSLog(@"Error at CVOpenGLESTextureCacheCreate: %d", err);
        ret = 0;
    }
    
    return ret;
}

- (void*)createTextureFromCVTextureCache:(void*)cache image:(void*)image width:(unsigned)w height:(unsigned)h
{
    void* texture = 0;
    
    CVReturn err = 0;
    if(_renderingAPI == apiMetal)
    {
        err = CVMetalTextureCacheCreateTextureFromImage(
                                                        kCFAllocatorDefault, (CVMetalTextureCacheRef)cache, (CVImageBufferRef)image, 0,
                                                        MTLPixelFormatBGRA8Unorm, w, h, 0, (CVMetalTextureRef*)&texture
                                                        );
    }
    else
    {
        err = CVOpenGLESTextureCacheCreateTextureFromImage(
                                                           kCFAllocatorDefault, (CVOpenGLESTextureCacheRef)cache, (CVImageBufferRef)image, 0,
                                                           GL_TEXTURE_2D, GL_RGBA, w, h, GL_BGRA_EXT, GL_UNSIGNED_BYTE,
                                                           0, (CVOpenGLESTextureRef*)&texture
                                                           );
    }
    
    if (err)
    {
        NSLog(@"Error at CVOpenGLESTextureCacheCreateTextureFromImage: %d", err);
        texture = 0;
    }
    
    return texture;
}

- (void)flushCVTextureCache:(void*)cache
{
    if(_renderingAPI == apiMetal)
        CVMetalTextureCacheFlush((CVMetalTextureCacheRef)cache, 0);
    else
        CVOpenGLESTextureCacheFlush((CVOpenGLESTextureCacheRef)cache, 0);
}
@end

static std::vector<UniversalMediaPlayer*> _players;

static NSString* CreateNSString(const char* string)
{
    if (string != NULL)
        return [NSString stringWithUTF8String:string];
    else
        return [NSString stringWithUTF8String:""];
}

extern "C"
{
    int UMPNativeInit()
    {
        _players.push_back([[UniversalMediaPlayer alloc] init]);
        return (int)_players.size();
    }
    
    void UMPNativeInitPlayer(int index, char *options, bool native)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        NSString *optionsString = CreateNSString(options);
        [player setupPlayer:optionsString];
    }
    
    int UMPGetBufferingPercentage()
    {
        return 0;
    }
    
    intptr_t UMPNativeGetTexturePointer(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        if (player.textureCache == nil)
            player.textureCache = [player createCVTextureCache];
        else
            [player flushCVTextureCache:player.textureCache];
        
        CVPixelBufferRef textureBuffer = [player videoBuffer];
        
        if (textureBuffer != nil)
        {
            if(player.texture)
                CFRelease(player.texture);
            
            player.texture = [player createTextureFromCVTextureCache:player.textureCache image:textureBuffer width:[player getVideoWidth] height:[player getVideoHeight]];
        }
        
        return [player getGLTextureFromCVTextureCache:player.texture];
    }
    
    void UMPNativeUpdateTexture(int index, intptr_t texture) {}
    
    void UMPNativeSetPixelsBuffer(int index, unsigned char *buffer, int width, int height)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        player.frameBuffer = buffer;
    }
    
    void UMPNativeUpdateFrameBuffer(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        if (kCVReturnSuccess == CVPixelBufferLockBaseAddress(player.videoBuffer, kCVPixelBufferLock_ReadOnly))
		{
            unsigned char *tmpBuffer = (uint8_t*)CVPixelBufferGetBaseAddress(player.videoBuffer);
            memcpy(player.frameBuffer, tmpBuffer, player.getVideoWidth * player.getVideoHeight * 4);
            CVPixelBufferUnlockBaseAddress(player.videoBuffer, kCVPixelBufferLock_ReadOnly);
        }
    }
    
    void UMPSetDataSource(int index, char *path)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        NSString *pathString = CreateNSString(path);
        [player setDataSource:pathString];
    }
    
    bool UMPPlay(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player play];
        return true;
    }
    
    void UMPPause(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player pause];
    }
    
    void UMPStop(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player stop];
    }
    
    void UMPRelease(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player free];
    }
	
	bool UMPIsPlaying(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player isPlaying];
    }
    
    bool UMPIsReady(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player isReady];
    }
    
    int UMPGetLength(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getDuration];
    }
	
	int UMPGetTime(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getTime];
    }
    
    void UMPSetTime(int index, int time)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player setTime:time];
    }
    
    float UMPGetPosition(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getPosition];
    }
    
    void UMPSetPosition(int index, float position)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player setPosition:position];
    }
	
	float UMPGetRate(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getPlaybackRate];
    }
    
	void UMPSetRate(int index, float rate)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        if (rate != player.getPlaybackRate)
            [player setPlaybackRate:rate];
    }
    
    int UMPGetVolume(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getVolume];
    }
    
    void UMPSetVolume(int index, int value)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        [player setVolume:value];
    }
	
	bool UMPGetMute(UniversalMediaPlayer *mpObj)
    {
        return false;
    }
	
	void UMPSetMute(int index, bool state)
    {
    }
    
    int UMPVideoWidth(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getVideoWidth];
    }
    
    int UMPVideoHeight(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        return [player getVideoHeight];
    }
    
    long UMPVideoFrameCount(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        if (player.framesCounter > 0)
        {
            if (player.cachedVolume >= 0)
            {
                [player setVolume:player.cachedVolume];
                player.cachedVolume = -1;
            }
        
            if (player.cachedRate >= 0)
            {
                [player setPlaybackRate:player.cachedRate];
                player.cachedRate = -1;
            }
        }
        
        return [player framesCounter];
    }
	
	int UMPGetState(int index)
	{
        UniversalMediaPlayer *player = _players.at(index - 1);
        
        if (player.playerStates.count > 0)
        {
            player.playerState = player.playerStates.queuePop;
            return player.playerState.state;
        }
        
        return Empty;
	}
    
    float UMPGetStateFloatValue(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        return player.playerState.valueFloat;
    }
    
    long UMPGetStateLongValue(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        return player.playerState.valueLong;
    }
    
    char* UMPGetStateStringValue(int index)
    {
        UniversalMediaPlayer *player = _players.at(index - 1);
        return player.playerState.valueString;
    }
}
