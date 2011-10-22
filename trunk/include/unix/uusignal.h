#ifndef UUSIGNAL_H
#define UUSIGNAL_H

#include <signal.h>
//#include <unix/uuprocess.h>

/**
 * \ingroup Unix
 * @{
**/

struct uuprocess;

//! Machdep func to deliver signal to user thread
errno_t sig_deliver( struct uuprocess *u, struct trap_state *st, int nsig, void *handler );

//! Init signal_handling_t struct
void sig_init( signal_handling_t *sh );

//! Send a signal (turn bit on)
void sig_send(signal_handling_t *sh, int signal );

//! Translate signals to user - internal, remove from here?
void sig_exec( struct uuprocess *u, signal_handling_t *sh, struct trap_state *st);

//! Translate signals to user
void execute_signals( struct uuprocess *u, struct trap_state *st);

void sig_send_to_current(int signal);

#endif // UUSIGNAL_H
