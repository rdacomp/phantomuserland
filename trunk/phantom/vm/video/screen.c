/**
 *
 * Phantom OS
 *
 * Copyright (C) 2005-2009 Dmitry Zavalishin, dz@dz.ru
 *
 * Kernel ready: yes
 * Preliminary: yes (needs cleanup and, possibly, data structures modifiation)
 *
 *
**/

#include "drv_video_screen.h"

// Placeholder for absent drv methods
void drv_video_null() {}









//#if VIDEO_ZBUF


void drv_video_bitblt_forw(const struct rgba_t *from, int xpos, int ypos, int xsize, int ysize, zbuf_t zpos )
{
    drv_video_bitblt_worker( from, xpos, ypos, xsize, ysize, 0, zpos );
}


void drv_video_bitblt_rev(const struct rgba_t *from, int xpos, int ypos, int xsize, int ysize, zbuf_t zpos)
{
    drv_video_bitblt_worker( from, xpos, ypos, xsize, ysize, 1, zpos );
}

void 	drv_video_win_winblt(const drv_video_window_t *from, int xpos, int ypos, zbuf_t zpos)
{
    drv_video_bitblt_worker( from->pixel, xpos, ypos, from->xsize, from->ysize, 0, zpos );
}

void 	drv_video_win_winblt_rev(const drv_video_window_t *from, int xpos, int ypos, zbuf_t zpos)
{
    drv_video_bitblt_worker( from->pixel, xpos, ypos, from->xsize, from->ysize, 1, zpos );
}

/*
#else




void drv_video_bitblt_forw(const struct rgba_t *from, int xpos, int ypos, int xsize, int ysize)
{
    drv_video_bitblt_worker( from, xpos, ypos, xsize, ysize, 0 );
}


void drv_video_bitblt_rev(const struct rgba_t *from, int xpos, int ypos, int xsize, int ysize)
{
    drv_video_bitblt_worker( from, xpos, ypos, xsize, ysize, 1 );
}

void 	drv_video_win_winblt(const drv_video_window_t *from, int xpos, int ypos)
{
    drv_video_bitblt_worker( from->pixel, xpos, ypos, from->xsize, from->ysize, 0 );
}

void 	drv_video_win_winblt_rev(const drv_video_window_t *from, int xpos, int ypos)
{
    drv_video_bitblt_worker( from->pixel, xpos, ypos, from->xsize, from->ysize, 1 );
}

#endif
*/







void drv_video_readblt_forw( struct rgba_t *to, int xpos, int ypos, int xsize, int ysize)
{
    drv_video_bitblt_reader( to, xpos, ypos, xsize, ysize, 0 );
}


void drv_video_readblt_rev( struct rgba_t *to, int xpos, int ypos, int xsize, int ysize)
{
    drv_video_bitblt_reader( to, xpos, ypos, xsize, ysize, 1 );
}

